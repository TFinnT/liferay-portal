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

package com.liferay.portlet.usersadmin.action;

import com.liferay.portal.NoSuchOrganizationException;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.spring.osgi.OSGiBeanProperties;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Organization;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.security.membershippolicy.MembershipPolicyException;
import com.liferay.portal.service.OrganizationLocalServiceUtil;
import com.liferay.portal.service.UserGroupServiceUtil;
import com.liferay.portal.service.UserServiceUtil;
import com.liferay.portal.util.PortletKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/**
 * @author Brian Wing Shun Chan
 */
@OSGiBeanProperties(
	property = {
		"javax.portlet.name=" + PortletKeys.USERS_ADMIN,
		"mvc.command.name=/users_admin/edit_organization_assignments"
	},
	service = MVCActionCommand.class
)
public class EditOrganizationAssignmentsMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			if (cmd.equals("organization_user_groups")) {
				updateOrganizationUserGroups(actionRequest);
			}
			else if (cmd.equals("organization_users")) {
				updateOrganizationUsers(actionRequest);
			}

			if (Validator.isNotNull(cmd)) {
				String redirect = ParamUtil.getString(
					actionRequest, "assignmentsRedirect");

				actionRequest.setAttribute(WebKeys.REDIRECT, redirect);
			}
		}
		catch (Exception e) {
			if (e instanceof MembershipPolicyException) {
				SessionErrors.add(actionRequest, e.getClass(), e);
			}
			else if (e instanceof NoSuchOrganizationException ||
					 e instanceof PrincipalException) {

				SessionErrors.add(actionRequest, e.getClass());

				actionResponse.setRenderParameter(
					"mvcPath", "/html/portlet/users_admin/error.jsp");
			}
			else {
				throw e;
			}
		}
	}

	protected void updateOrganizationUserGroups(ActionRequest actionRequest)
		throws Exception {

		long organizationId = ParamUtil.getLong(
			actionRequest, "organizationId");

		Organization organization =
			OrganizationLocalServiceUtil.getOrganization(organizationId);

		long groupId = organization.getGroupId();

		long[] addUserGroupIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "addUserGroupIds"), 0L);
		long[] removeUserGroupIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "removeUserGroupIds"), 0L);

		UserGroupServiceUtil.addGroupUserGroups(groupId, addUserGroupIds);
		UserGroupServiceUtil.unsetGroupUserGroups(groupId, removeUserGroupIds);
	}

	protected void updateOrganizationUsers(ActionRequest actionRequest)
		throws Exception {

		long organizationId = ParamUtil.getLong(
			actionRequest, "organizationId");

		long[] addUserIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "addUserIds"), 0L);
		long[] removeUserIds = StringUtil.split(
			ParamUtil.getString(actionRequest, "removeUserIds"), 0L);

		UserServiceUtil.addOrganizationUsers(organizationId, addUserIds);
		UserServiceUtil.unsetOrganizationUsers(organizationId, removeUserIds);
	}

}