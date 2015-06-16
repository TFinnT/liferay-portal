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

package com.liferay.portal.kernel.security.access.control;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;
import com.liferay.portal.kernel.util.AutoResetThreadLocal;
import com.liferay.portal.security.auth.AccessControlContext;
import com.liferay.portal.security.auth.AuthException;
import com.liferay.portal.util.PortalUtil;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Tomas Polesovsky
 * @author Michael C. Han
 * @author Raymond Augé
 */
public class AccessControlUtil {

	public static AccessControl getAccessControl() {
		PortalRuntimePermission.checkGetBeanProperty(AccessControlUtil.class);

		return _accessControl;
	}

	public static AccessControlContext getAccessControlContext() {
		return _accessControlContext.get();
	}

	public static void initAccessControlContext(
		HttpServletRequest request, HttpServletResponse response,
		Map<String, Object> settings) {

		getAccessControl().initAccessControlContext(
			request, response, settings);
	}

	public static void initContextUser(long userId) throws AuthException {
		getAccessControl().initContextUser(userId);
	}

	public static boolean isAccessAllowed(
		HttpServletRequest request, Set<String> hostsAllowed) {

		if (hostsAllowed.isEmpty()) {
			return true;
		}

		String remoteAddr = request.getRemoteAddr();

		if (hostsAllowed.contains(remoteAddr)) {
			return true;
		}

		String computerAddress = PortalUtil.getComputerAddress();

		if (computerAddress.equals(remoteAddr) &&
			hostsAllowed.contains(_SERVER_IP)) {

			return true;
		}

		return false;
	}

	public static void setAccessControlContext(
		AccessControlContext accessControlContext) {

		_accessControlContext.set(accessControlContext);
	}

	public static AuthVerifierResult.State verifyRequest()
		throws PortalException {

		return getAccessControl().verifyRequest();
	}

	public void setAccessControl(AccessControl accessControl) {
		PortalRuntimePermission.checkSetBeanProperty(getClass());

		_accessControl = accessControl;
	}

	private static final String _SERVER_IP = "SERVER_IP";

	private static AccessControl _accessControl;
	private static final ThreadLocal<AccessControlContext>
		_accessControlContext = new AutoResetThreadLocal<>(
			AccessControlUtil.class + "._accessControlContext");

}