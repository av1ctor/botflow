package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionAuth;
import com.robotikflow.core.models.entities.CollectionAuthRole;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.response.GroupBaseResponse;
import com.robotikflow.core.models.response.UserBaseResponse;

public class CollectionAuthBaseResponse 
{
	private final String id;
	private final UserBaseResponse user;
	private final GroupBaseResponse group;
	private final CollectionAuthRole role;
	private final int roleNum;
	private final boolean reverse;	

	public CollectionAuthBaseResponse(
		final CollectionAuth auth, 
		final Workspace workspace)
	{
		id = auth.getPubId();
		user = auth.getUser() != null? 
			new UserBaseResponse(auth.getUser()): 
			null;
		group = auth.getGroup() != null? 
			new GroupBaseResponse(auth.getGroup()): 
			null;
		role = auth.getRole();
		roleNum = auth.getRole().getValue();
		reverse = auth.isReverse();
	}

	public String getId() {
		return id;
	}

	public UserBaseResponse getUser() {
		return user;
	}

	public GroupBaseResponse getGroup() {
		return group;
	}

	public CollectionAuthRole getRole() {
		return role;
	}

	public int getRoleNum() {
		return roleNum;
	}

	public boolean isReverse() {
		return reverse;
	}
}
