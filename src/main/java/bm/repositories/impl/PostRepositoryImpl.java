package bm.repositories.impl;

import bm.exceptions.UnknownException;
import bm.models.Category;
import bm.models.Post;
import bm.models.Tag;
import bm.repositories.interfaces.PostRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PostRepositoryImpl extends PostgreSqlAbstractRepository implements PostRepository {

    @Override
    public Post addPost(Post post) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.newConnection();

            String[] generatedColumns = {"id"};

            preparedStatement = connection.prepareStatement("INSERT INTO post(title, text, author, created_at, number_of_visits, category_id)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?)\n" +
                    "RETURNING *;", generatedColumns);
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getText());
            preparedStatement.setString(3, post.getAuthor());
            preparedStatement.setLong(4, System.currentTimeMillis());
            preparedStatement.setLong(5, 0);
            preparedStatement.setLong(6, post.getCategory().getId());

            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                post.setId(resultSet.getLong("id"));
                post.setCreatedAt(resultSet.getLong("created_at"));
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            throw new UnknownException();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeResultSet(resultSet);
            this.closeConnection(connection);
        }
        return post;
    }

    @Override
    public Post updatePost(Post post) {
        return null;
    }

    @Override
    public List<Post> listAllPosts(int offset, int limit) {
        List<Post> posts = new ArrayList<>();
        int postId = 1, postTitle = 2, postText = 3, postAuthor = 4, postCreatedAt = 5, postNumberOfVisits = 6,
                tagValue = 9, categoryId = 10, categoryName = 11, categoryDescription = 12;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement("SELECT p.*, t.*, c.* \n" +
                    "FROM(\n" +
                    "\t\tpost_tag as pt \n" +
                    "\t\tFULL JOIN post as p \n" +
                    "\t\t\ton pt.post_id = p.id \n" +
                    "\t\tLEFT JOIN tag as t \n" +
                    "\t\t\ton pt.tag_id = t.id\n" +
                    "\t) \n" +
                    "LEFT JOIN category as c \n" +
                    "\ton p.category_id = c.id\n" +
                    "order by p.created_at desc\n" +
                    "OFFSET ? LIMIT ?");
            preparedStatement.setLong(1, offset);
            preparedStatement.setLong(2, limit);
            resultSet = preparedStatement.executeQuery();

            Map<Long, List<String>> postTags = new HashMap<>();

            while (resultSet.next()) {
                long currentPostId = resultSet.getLong(postId);

                if (!postTags.containsKey(currentPostId)) {
                    postTags.put(currentPostId, Collections.singletonList(resultSet.getString(tagValue)));

                    Category category = new Category(resultSet.getLong(categoryId), resultSet.getString(categoryName),
                            resultSet.getString(categoryDescription), null);

                    posts.add(new Post(resultSet.getLong(postId), resultSet.getString(postTitle), resultSet.getString(postText),
                            resultSet.getString(postAuthor), resultSet.getLong(postCreatedAt),
                            resultSet.getLong(postNumberOfVisits), category, null, null));
                } else {
                    postTags.get(currentPostId).add(resultSet.getString(tagValue));
                }
            }

            for (Post post : posts){
                post.setTagsString(String.join(", ", postTags.get(post.getId())));
            }

        } catch (Exception e) {
            throw new UnknownException();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeResultSet(resultSet);
            this.closeConnection(connection);
        }
        return posts;
    }

    @Override
    public List<Post> listPostsByTags(long tag_id) {
        return null;
    }

    @Override
    public void deletePost(long post_id) {

    }
}
