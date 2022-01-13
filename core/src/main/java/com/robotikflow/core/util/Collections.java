package com.robotikflow.core.util;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Map;

public class Collections
{
	public static int removeEntriesWithKey(
		final List<String> list, 
		final String key) 
	{
		var index = list.indexOf(key);
		if(index >= 0)
		{
			list.remove(index);
			return 1;
		}

		return 0;
	}

	public static int removeEntriesWithPartialKey(
		final List<String> list, 
		final String partialKey) 
	{
		var cnt = 0;

		var matches = list.stream()
		.filter(k -> k.indexOf(partialKey) == 0)
			.collect(Collectors.toList());

		for(var match : matches)
		{
			list.remove(match);
			++cnt;
		}		

		return cnt;
	}

	public static int removeEntriesWithKey(
		final Set<String> set, 
		final String key) 
	{
		if(set.contains(key))
		{
			set.remove(key);
			return 1;
		}

		return 0;
	}

	public static int removeEntriesWithPartialKey(
		final Set<String> list, 
		final String partialKey) 
	{
		var cnt = 0;

		var matches = list.stream()
		.filter(k -> k.indexOf(partialKey) == 0)
			.collect(Collectors.toList());

		for(var match : matches)
		{
			list.remove(match);
			++cnt;
		}		

		return cnt;
	}

	public static <T> int removeEntriesWithKey(
		final Map<String, T> map, 
		final String key) 
	{
		if(map.containsKey(key))
		{
			map.remove(key);
			return 1;
		}

		return 0;
	}

	public static <T> int removeEntriesWithKey(
		final Map<String, T> map, 
		final String key, 
		final Predicate<T> checkPredicate) 
	{
		if(map.containsKey(key))
		{
			var obj = map.get(key);
			if(checkPredicate.test(obj))
			{
				map.remove(key);
				return 1;
			}
		}

		return 0;
	}

	public static <T> int removeEntriesWithPartialKey(
		final Map<String, T> map, 
		final String partialKey) 
	{
		var cnt = 0;
		
		var matches = map.keySet().stream()
		.filter(k -> k.indexOf(partialKey) == 0)
			.collect(Collectors.toList());

		for(var match : matches)
		{
			map.remove(match);
			++cnt;
		}

		return cnt;
	}	

	public static <T> int removeEntriesWithPartialKey(
		final Map<String, T> map, 
		final String partialKey,
		final Predicate<T> checkPredicate) 
	{
		var cnt = 0;
		
		var matches = map.keySet().stream()
		.filter(k -> k.indexOf(partialKey) == 0)
			.collect(Collectors.toList());

		for(var match : matches)
		{
			var obj = map.get(match);
			if(checkPredicate.test(obj))
			{
				map.remove(match);
				++cnt;
			}
		}

		return cnt;
    }    
    
	public static int renameEntriesWithKey(
		final List<String> list, 
		final String curKey, 
		final String newKey) 
	{
		if(list.contains(curKey))
		{
			list.add(newKey);
			list.remove(curKey);
			return 1;
		}

		return 0;
	}

	public static int renameEntriesWithKey(
		final Set<String> set, 
		final String curKey, 
		final String newKey) 
	{
		if(set.contains(curKey))
		{
			set.add(newKey);
			set.remove(curKey);
			return 1;
		}

		return 0;
	}

	public static <T> int renameEntriesWithKey(
		final Map<String, T> map, 
		final String curKey, 
		final String newKey) 
	{
		if(map.containsKey(curKey))
		{
			map.put(newKey, map.get(curKey));
			map.remove(curKey);
			return 1;
		}

		return 0;
	}

	public static <T> int renameEntriesWithKey(
		final Map<String, T> map, 
		final String curKey, 
		final String newKey,
		final Predicate<T> checkPredicate) 
	{
		if(map.containsKey(curKey))
		{
			var obj = map.get(curKey);
			if(checkPredicate.test(obj))
			{
				map.put(newKey, obj);
				map.remove(curKey);
				return 1;
			}
		}

		return 0;
	}	

	public static int renameEntriesWithPartialKey(
		final List<String> list,
		final String partialKey,
		final String curKey,
		final String newKey)
	{
		var changes = 0;

		var matched = list.stream()
			.filter(key -> key.indexOf(partialKey) == 0)
				.collect(Collectors.toList());

		for(var match : matched)
		{
			list.add(match.replace(curKey, newKey));
			list.remove(match);
			++changes;
		}

		return changes;
	}	

	public static int renameEntriesWithPartialKey(
		final Set<String> set,
		final String partialKey,
		final String curKey,
		final String newKey)
	{
		var changes = 0;

		var matched = set.stream()
			.filter(key -> key.indexOf(partialKey) == 0)
				.collect(Collectors.toList());

		for(var match : matched)
		{
			set.add(match.replace(curKey, newKey));
			set.remove(match);
			++changes;
		}

		return changes;
	}	

	public static <T> int renameEntriesWithPartialKey(
		final Map<String, T> map,
		final String partialKey,
		final String curKey,
		final String newKey)
	{
		var changes = 0;

		var matched = map.keySet().stream()
			.filter(key -> key.indexOf(partialKey) == 0)
				.collect(Collectors.toList());

		for(var match : matched)
		{
			map.put(match.replace(curKey, newKey), map.get(match));
			map.remove(match);
			++changes;
		}

		return changes;
	}

	public static <T> int renameEntriesWithPartialKey(
		final Map<String, T> map,
		final String partialKey,
		final String curKey,
		final String newKey,
		final Predicate<T> checkPredicate)
	{
		var changes = 0;

		var matched = map.keySet().stream()
			.filter(key -> key.indexOf(partialKey) == 0)
				.collect(Collectors.toList());

		for(var match : matched)
		{
			var obj = map.get(match);
			if(checkPredicate.test(obj))
			{
				map.put(match.replace(curKey, newKey), obj);
				map.remove(match);
				++changes;
			}
		}

		return changes;
	}
}