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

package com.liferay.portal.service.permission;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.model.Team;
import com.liferay.portal.security.permission.PermissionChecker;

/**
 * @author Brian Wing Shun Chan
 */
public interface TeamPermission {

	public void check(
			PermissionChecker permissionChecker, long teamId, String actionId)
		throws PortalException;

	public void check(
			PermissionChecker permissionChecker, Team team, String actionId)
		throws PortalException;

	public boolean contains(
			PermissionChecker permissionChecker, long teamId, String actionId)
		throws PortalException;

	public boolean contains(
			PermissionChecker permissionChecker, Team team, String actionId)
		throws PortalException;

}