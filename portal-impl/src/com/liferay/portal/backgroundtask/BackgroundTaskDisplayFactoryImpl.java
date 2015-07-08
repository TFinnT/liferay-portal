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

package com.liferay.portal.backgroundtask;

import com.liferay.portal.kernel.backgroundtask.BackgroundTaskDisplay;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskDisplayFactory;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.BackgroundTask;
import com.liferay.portal.service.BackgroundTaskLocalServiceUtil;

/**
 * @author Andrew Betts
 */
public class BackgroundTaskDisplayFactoryImpl
	implements BackgroundTaskDisplayFactory {

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		BackgroundTask backgroundTask) {

		if (backgroundTask == null) {
			return null;
		}

		BackgroundTaskExecutor backgroundTaskExecutor =
			backgroundTask.getBackgroundTaskExecutor();

		return backgroundTaskExecutor.getBackgroundTaskDisplay(backgroundTask);
	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(
		long backgroundTaskId) {

		BackgroundTask backgroundTask =
			BackgroundTaskLocalServiceUtil.fetchBackgroundTask(
				backgroundTaskId);

		return getBackgroundTaskDisplay(backgroundTask);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BackgroundTaskDisplayFactoryImpl.class);

}