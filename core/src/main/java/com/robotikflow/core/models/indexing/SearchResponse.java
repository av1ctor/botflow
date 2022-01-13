package com.robotikflow.core.models.indexing;

public class SearchResponse 
{
	private final String id;
	private final float score;
	private final String highlightText;

	public SearchResponse(String id, float score, String highlightText) 
	{
		this.id = id;
		this.score = score;
		this.highlightText = highlightText;
	}

	public String getId() {
		return id;
	}

	public float getScore() {
		return score;
	}

	public String getHighlightText() {
		return highlightText;
	}
}
