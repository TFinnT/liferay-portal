<%--
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
--%>

<%@ include file="/html/portlet/users_admin/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

String backURL = ParamUtil.getString(request, "backURL", redirect);

User selUser = PortalUtil.getSelectedUser(request);

Contact selContact = null;

if (selUser != null) {
	selContact = selUser.getContact();
}

PasswordPolicy passwordPolicy = null;

if (selUser == null) {
	passwordPolicy = PasswordPolicyLocalServiceUtil.getDefaultPasswordPolicy(company.getCompanyId());
}
else {
	passwordPolicy = selUser.getPasswordPolicy();
}

List<Group> groups = Collections.emptyList();

if (selUser != null) {
	groups = selUser.getGroups();

	if (filterManageableGroups) {
		groups = UsersAdminUtil.filterGroups(permissionChecker, groups);
	}
}

List<Organization> organizations = Collections.emptyList();

if (selUser != null) {
	organizations = selUser.getOrganizations();
}

if (filterManageableOrganizations) {
	organizations = UsersAdminUtil.filterOrganizations(permissionChecker, organizations);
}

List<Role> roles = Collections.emptyList();

if (selUser != null) {
	roles = selUser.getRoles();

	if (filterManageableRoles) {
		roles = UsersAdminUtil.filterRoles(permissionChecker, roles);
	}
}

List<UserGroupRole> organizationRoles = new ArrayList<UserGroupRole>();
List<UserGroupRole> siteRoles = new ArrayList<UserGroupRole>();

List<UserGroupRole> userGroupRoles = Collections.emptyList();

if (selUser != null) {
	userGroupRoles = UserGroupRoleLocalServiceUtil.getUserGroupRoles(selUser.getUserId());

	if (filterManageableUserGroupRoles) {
		userGroupRoles = UsersAdminUtil.filterUserGroupRoles(permissionChecker, userGroupRoles);
	}
}

for (UserGroupRole userGroupRole : userGroupRoles) {
	int roleType = userGroupRole.getRole().getType();

	if (roleType == RoleConstants.TYPE_ORGANIZATION) {
		organizationRoles.add(userGroupRole);
	}
	else if (roleType == RoleConstants.TYPE_SITE) {
		siteRoles.add(userGroupRole);
	}
}

List<UserGroup> userGroups = Collections.emptyList();

if (selUser != null) {
	userGroups = selUser.getUserGroups();

	if (filterManageableUserGroups) {
		userGroups = UsersAdminUtil.filterUserGroups(permissionChecker, userGroups);
	}
}

List<UserGroupGroupRole> inheritedSiteRoles = Collections.emptyList();

if (selUser != null) {
	inheritedSiteRoles = UserGroupGroupRoleLocalServiceUtil.getUserGroupGroupRolesByUser(selUser.getUserId());
}

List<Group> inheritedSites = GroupLocalServiceUtil.getUserGroupsRelatedGroups(userGroups);
List<Group> organizationsRelatedGroups = Collections.emptyList();

if (!organizations.isEmpty()) {
	organizationsRelatedGroups = GroupLocalServiceUtil.getOrganizationsRelatedGroups(organizations);

	for (Group group : organizationsRelatedGroups) {
		if (!inheritedSites.contains(group)) {
			inheritedSites.add(group);
		}
	}
}

List<Group> allGroups = new ArrayList<Group>();

allGroups.addAll(groups);
allGroups.addAll(inheritedSites);
allGroups.addAll(organizationsRelatedGroups);
allGroups.addAll(GroupLocalServiceUtil.getOrganizationsGroups(organizations));
allGroups.addAll(GroupLocalServiceUtil.getUserGroupsGroups(userGroups));

List<Group> roleGroups = new ArrayList<Group>();

for (Group group : allGroups) {
	if (RoleLocalServiceUtil.hasGroupRoles(group.getGroupId())) {
		roleGroups.add(group);
	}
}

if (organizations.size() == 1) {
	UsersAdminUtil.addPortletBreadcrumbEntries(organizations.get(0), request, renderResponse);
}

if (selUser != null) {
	if (!portletName.equals(PortletKeys.MY_ACCOUNT)) {
		PortalUtil.addPortletBreadcrumbEntry(request, selUser.getFullName(), null);
	}
}
%>

<liferay-ui:error exception="<%= CompanyMaxUsersException.class %>" message="unable-to-create-user-account-because-the-maximum-number-of-users-has-been-reached" />

<c:if test="<%= !portletName.equals(PortletKeys.MY_ACCOUNT) %>">
	<aui:nav-bar>
		<liferay-util:include page="/html/portlet/users_admin/toolbar.jsp">
			<liferay-util:param name="toolbarItem" value='<%= (selUser == null) ? "add" : "view" %>' />
		</liferay-util:include>
	</aui:nav-bar>

	<div id="breadcrumb">
		<liferay-ui:breadcrumb showCurrentGroup="<%= false %>" showGuestGroup="<%= false %>" showLayout="<%= false %>" showPortletBreadcrumb="<%= true %>" />
	</div>

	<liferay-ui:header
		backURL="<%= backURL %>"
		escapeXml="<%= false %>"
		localizeTitle="<%= (selUser == null) %>"
		title='<%= (selUser == null) ? "add-user" : LanguageUtil.format(request, "edit-user-x", HtmlUtil.escape(selUser.getFullName()), false) %>'
	/>
</c:if>

<portlet:actionURL var="editUserActionURL">
	<portlet:param name="struts_action" value="/users_admin/edit_user" />
</portlet:actionURL>

<portlet:renderURL var="editUserRenderURL">
	<portlet:param name="struts_action" value="/users_admin/edit_user" />
	<portlet:param name="backURL" value="<%= backURL %>" />
</portlet:renderURL>

<aui:form action="<%= editUserActionURL %>" method="post" name="fm">
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= (selUser == null) ? Constants.ADD : Constants.UPDATE %>" />
	<aui:input name="redirect" type="hidden" value="<%= editUserRenderURL %>" />
	<aui:input name="backURL" type="hidden" value="<%= backURL %>" />
	<aui:input name="p_u_i_d" type="hidden" value="<%= (selUser != null) ? selUser.getUserId() : 0 %>" />

	<%
	request.setAttribute("user.selUser", selUser);
	request.setAttribute("user.selContact", selContact);
	request.setAttribute("user.passwordPolicy", passwordPolicy);
	request.setAttribute("user.groups", groups);
	request.setAttribute("user.inheritedSites", inheritedSites);
	request.setAttribute("user.organizations", organizations);
	request.setAttribute("user.roles", roles);
	request.setAttribute("user.organizationRoles", organizationRoles);
	request.setAttribute("user.siteRoles", siteRoles);
	request.setAttribute("user.inheritedSiteRoles", inheritedSiteRoles);
	request.setAttribute("user.userGroups", userGroups);
	request.setAttribute("user.allGroups", allGroups);
	request.setAttribute("user.roleGroups", roleGroups);

	request.setAttribute("addresses.className", Contact.class.getName());
	request.setAttribute("emailAddresses.className", Contact.class.getName());
	request.setAttribute("phones.className", Contact.class.getName());
	request.setAttribute("websites.className", Contact.class.getName());

	if (selContact != null) {
		request.setAttribute("addresses.classPK", selContact.getContactId());
		request.setAttribute("emailAddresses.classPK", selContact.getContactId());
		request.setAttribute("phones.classPK", selContact.getContactId());
		request.setAttribute("websites.classPK", selContact.getContactId());
	}
	else {
		request.setAttribute("addresses.classPK", 0L);
		request.setAttribute("emailAddresses.classPK", 0L);
		request.setAttribute("phones.classPK", 0L);
		request.setAttribute("websites.classPK", 0L);
	}
	%>

	<liferay-util:buffer var="htmlTop">
		<c:if test="<%= selUser != null %>">
			<div class="user-info">
				<div class="float-container">
					<img alt="<%= HtmlUtil.escapeAttribute(selUser.getFullName()) %>" class="user-logo" src="<%= selUser.getPortraitURL(themeDisplay) %>" />

					<span class="user-name"><%= HtmlUtil.escape(selUser.getFullName()) %></span>
				</div>
			</div>
		</c:if>
	</liferay-util:buffer>

	<liferay-util:buffer var="htmlBottom">

		<%
		boolean lockedOut = false;

		if ((selUser != null) && (passwordPolicy != null)) {
			try {
				UserLocalServiceUtil.checkLockout(selUser);
			}
			catch (UserLockoutException.PasswordPolicyLockout ule) {
				lockedOut = true;
			}
		}
		%>

		<c:if test="<%= lockedOut %>">
			<aui:button-row>
				<div class="alert alert-warning"><liferay-ui:message key="this-user-account-has-been-locked-due-to-excessive-failed-login-attempts" /></div>

				<%
				String taglibOnClick = renderResponse.getNamespace() + "saveUser('unlock');";
				%>

				<aui:button onClick="<%= taglibOnClick %>" value="unlock" />
			</aui:button-row>
		</c:if>
	</liferay-util:buffer>

	<liferay-ui:form-navigator
		backURL="<%= backURL %>"
		formModelBean="<%= selUser %>"
		htmlBottom="<%= htmlBottom %>"
		htmlTop="<%= htmlTop %>"
		id="<%= FormNavigatorConstants.FORM_NAVIGATOR_ID_USERS %>"
	/>
</aui:form>

<%
if (selUser != null) {
	PortalUtil.setPageSubtitle(selUser.getFullName(), request);
}
%>

<aui:script>
	function <portlet:namespace />createURL(href, value, onclick) {
		return '<a href="' + href + '"' + (onclick ? ' onclick="' + onclick + '" ' : '') + '>' + value + '</a>';
	}

	function <portlet:namespace />saveUser(cmd) {
		document.<portlet:namespace />fm.<portlet:namespace /><%= Constants.CMD %>.value = cmd;

		submitForm(document.<portlet:namespace />fm);
	}
</aui:script>