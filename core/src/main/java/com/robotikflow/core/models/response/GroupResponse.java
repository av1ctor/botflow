package com.robotikflow.core.models.response;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.Group;

public class GroupResponse
	extends GroupBaseResponse
{
	private final String parentId;
	private final List<String> children;

    public GroupResponse(
		final Group group,
		final Function<Group, List<Group>> findChildren) 
	{
    	super(group);
		
		parentId = findChildren != null && group.getParent() != null?
			group.getParent().getPubId():
			null;

		var desc = findChildren != null?
			findChildren.apply(group):
			null;

		children = desc != null && desc.size() > 0?
			desc.stream()
				.map(g -> g.getPubId())
				.collect(Collectors.toList()):
			null;
    }

    public GroupResponse(
		final Group group) 
	{
		this(group, null);
	}

	public String getParentId() {
		return parentId;
	}

	public List<String> getChildren() {
		return children;
	}
}

