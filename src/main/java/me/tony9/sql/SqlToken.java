package me.tony9.sql;

/**
 * Created by Tony on 2017/12/19.
 */
public class SqlToken {

    private String text;
    private int startPos;
    private int endPos;


    public SqlToken() {
        this.text = null;
        this.startPos = -1;
        this.endPos = -1;
    }

    public SqlToken(String text, int startPos, int endPos) {
        this.text = text;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String toString() {
        return this.text;
    }

//    //合并连续的Token
//    public void mergeNext(SqlToken token) {
//        if (this.text == null) {
//            this.text = token.text;
//            this.startPos = token.startPos;
//            this.endPos = token.endPos;
//        } else {
//            if (this.endPos <= token.startPos) {
//                this.text = this.text + " " + token.text;
//                this.endPos = token.endPos;
//            } else {
//                throw new RuntimeException(String.format("只有连续的Token才能合并！(%s, %s)", this.text, token.text));
//            }
//        }
//    }
}
