/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.lar.backgroundtask;

import com.liferay.portal.kernel.backgroundtask.BackgroundTaskDisplay;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatus;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatusRegistryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.BackgroundTask;
import com.liferay.portal.security.permission.ResourceActionsUtil;

import java.io.Serializable;

import java.util.Locale;
import java.util.Map;

/**
 * @author Andrew Betts
 */
public class StagingBackgroundTaskDisplay implements BackgroundTaskDisplay {

	public StagingBackgroundTaskDisplay(
		BackgroundTask backgroundTask, Locale locale) {

		_backgroundTaskStatus =
			BackgroundTaskStatusRegistryUtil.getBackgroundTaskStatus(
				backgroundTask.getBackgroundTaskId());

		_details = createDetailsJSON(locale, backgroundTask);

		Map<String, Serializable> taskContextMap =
			backgroundTask.getTaskContextMap();

		_cmd = (String)taskContextMap.get(Constants.CMD);

		if (_backgroundTaskStatus == null) {
			_allProgressBarCountersTotal = 0;
			_message = null;
			_percentage = 0;
			_stagedModelName = null;
			_stagedModelType = null;

			return;
		}

		long allModelAdditionCountersTotal = GetterUtil.getLong(
			_backgroundTaskStatus.getAttribute(
				"allModelAdditionCountersTotal"));
		long allPortletAdditionCounter = GetterUtil.getLong(
			_backgroundTaskStatus.getAttribute("allPortletAdditionCounter"));
		long currentModelAdditionCountersTotal = GetterUtil.getLong(
			_backgroundTaskStatus.getAttribute(
				"currentModelAdditionCountersTotal"));
		long currentPortletAdditionCounter = GetterUtil.getLong(
			_backgroundTaskStatus.getAttribute(
				"currentPortletAdditionCounter"));
		String phase = GetterUtil.getString(
			_backgroundTaskStatus.getAttribute("phase"));

		_allProgressBarCountersTotal =
			allModelAdditionCountersTotal + allPortletAdditionCounter;

		_percentage = calculatePercentage(
			currentModelAdditionCountersTotal, currentPortletAdditionCounter,
			phase);

		_stagedModelName = GetterUtil.getString(
			_backgroundTaskStatus.getAttribute("stagedModelName"));
		_stagedModelType = GetterUtil.getString(
			_backgroundTaskStatus.getAttribute("stagedModelType"));

		_message = processMessage(locale);
	}

	@Override
	public JSONObject getDetails() {
		return _details;
	}

	@Override
	public String getMessage() {
		return _message;
	}

	@Override
	public int getPercentage() {
		return _percentage;
	}

	@Override
	public boolean hasBackgroundTaskStatus() {
		if (_backgroundTaskStatus == null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean hasDetails() {
		if (_details == null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean hasMessage() {
		if (hasRemoteMessage() || hasStagedModelMessage()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean hasPercentage() {
		if ((_allProgressBarCountersTotal > 0) &&
			(!Validator.equals(_cmd, Constants.PUBLISH_TO_REMOTE) ||
			 (_percentage < 100))) {

			return true;
		}

		return false;
	}

	protected int calculatePercentage(
		long currentModelAdditionCountersTotal,
		long currentPortletAdditionCounter, String phase) {

		int percentage = 100;

		long currentProgressBarCountersTotal =
			currentModelAdditionCountersTotal + currentPortletAdditionCounter;

		if (_allProgressBarCountersTotal > 0) {
			int base = 100;

			if (phase.equals(Constants.EXPORT) &&
				!Validator.equals(_cmd, Constants.PUBLISH_TO_REMOTE)) {

				base = 50;
			}

			percentage = Math.round(
				(float)currentProgressBarCountersTotal /
					_allProgressBarCountersTotal * base);
		}

		return percentage;
	}

	protected boolean hasRemoteMessage() {
		if (Validator.equals(_cmd, Constants.PUBLISH_TO_REMOTE) &&
			(_percentage == 100)) {

			return true;
		}

		return false;
	}

	protected boolean hasStagedModelMessage() {
		if (Validator.isNotNull(_stagedModelName) &&
			Validator.isNotNull(_stagedModelType)) {

			return true;
		}

		return false;
	}

	protected String processMessage(Locale locale) {

		String messageKey = "exporting";

		if (Validator.equals(_cmd, Constants.IMPORT)) {
			messageKey = "importing";
		}
		else if (Validator.equals(_cmd, Constants.PUBLISH_TO_LIVE) ||
				 Validator.equals(_cmd, Constants.PUBLISH_TO_REMOTE)) {

			messageKey = "publishing";
		}

		String message = StringPool.BLANK;

		if (hasRemoteMessage()) {
			message = LanguageUtil.get(
				locale,
				"please-wait-as-the-publication-processes-on-the-remote-site");
		}

		if (hasStagedModelMessage()) {
			StringBundler sb = new StringBundler();

			sb.append("<strong>");
			sb.append(LanguageUtil.get(locale, messageKey));
			sb.append(StringPool.TRIPLE_PERIOD);
			sb.append("</strong>");
			sb.append(
				ResourceActionsUtil.getModelResource(locale, _stagedModelType));
			sb.append("<em>");
			sb.append(HtmlUtil.escape(_stagedModelName));
			sb.append("</em>");

			message = sb.toString();
		}

		return message;
	}

	private JSONObject createDetailsJSON(
		Locale locale, BackgroundTask backgroundTask) {

		JSONObject backgroundTaskJSON;

		try {
			backgroundTaskJSON = JSONFactoryUtil.createJSONObject(
				backgroundTask.getStatusMessage());
		}
		catch (Exception e) {
			return null;
		}

		boolean exported = MapUtil.getBoolean(
			backgroundTask.getTaskContextMap(), "exported");
		boolean validated = MapUtil.getBoolean(
			backgroundTask.getTaskContextMap(), "validated");

		String detailHeader = LanguageUtil.get(
			locale,
			"an-unexpected-error-occurred-with-the-publication-process." +
				"-please-check-your-portal-and-publishing-configuration");

		if (exported && !validated) {
			detailHeader = LanguageUtil.get(
				locale,
				"an-unexpected-error-occurred-with-the-publication-" +
					"process.-please-check-your-portal-and-publishing-" +
					"configuration");
		}

		JSONArray detailItems = JSONFactoryUtil.createJSONArray();

		JSONArray errorMessagesJSONArray = backgroundTaskJSON.getJSONArray(
			"messageListItems");

		if (errorMessagesJSONArray != null) {
			JSONObject errorDetails = JSONFactoryUtil.createJSONObject();

			errorDetails.put(
				"message", backgroundTaskJSON.getString("message"));
			errorDetails.put("itemsList", errorMessagesJSONArray);

			detailItems.put(errorDetails);
		}

		JSONArray warningMessagesJSONArray = backgroundTaskJSON.getJSONArray(
			"warningMessages");

		if (warningMessagesJSONArray != null) {
			String message = "the-following-data-has-not-been-published";

			if ((errorMessagesJSONArray != null) &&
				(errorMessagesJSONArray.length() > 0)) {

				message =
					"consider-that-the-following-data-would-not-have-been-" +
						"published-either";
			}

			JSONObject warningDetails = JSONFactoryUtil.createJSONObject();

			warningDetails.put("message", message);
			warningDetails.put("itemsList", warningMessagesJSONArray);

			detailItems.put(warningDetails);
		}

		JSONObject detailsJSON = JSONFactoryUtil.createJSONObject();

		detailsJSON.put("detailHeader", detailHeader);
		detailsJSON.put("detailItems", detailItems);
		detailsJSON.put("status", backgroundTaskJSON.getInt("status"));

		return detailsJSON;
	}

	private final long _allProgressBarCountersTotal;
	private final BackgroundTaskStatus _backgroundTaskStatus;
	private final String _cmd;
	private final JSONObject _details;
	private final String _message;
	private int _percentage;
	private final String _stagedModelName;
	private final String _stagedModelType;

}