package luckynine.youtell.data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Weiliang on 6/20/2015.
 */
public class Post {

    public String _id;

    public String author;

    public String title;

    public String content;

    public Timestamp createdAt;

    public Post(){
        createdAt = new Timestamp(new Date().getTime());
    }

    public String getId() {
        return this._id;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getTitle(){
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public Date getCreatedAt(){
        return this.createdAt;
    }
}
