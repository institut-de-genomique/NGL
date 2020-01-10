package controllers;

import java.util.List;

import models.laboratory.common.instance.Comment;

/**
 * Object that maintains a list of comments.
 * 
 * @author vrd
 *
 */
// Probably CommentsHolder would be a better name.
public interface ICommentable {

	/**
	 * Get the comments.
	 * @return comments
	 */
	List<Comment> getComments();
	
	/**
	 * Set the comments.
	 * @param comments new comments
	 */
	void setComments(List<Comment> comments);
	
}
