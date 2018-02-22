package me.tony9.sql;

import java.util.Arrays;
import java.util.List;

public class SqlFormat {


    private static String PADDING = ">>";
    private static String NEWLINE = System.getProperty("line.separator");



    public SqlFormat() {
    }




    private StringBuffer pad(int nIndet) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < nIndet; i ++) s.append(PADDING);
        return s;
    }

//    public String format(String sql) {
//
//        int nDepth = 0;             //SQL语句嵌套深度
//        int nIndent = 0;            //按SQL关键字切分的深度
//        boolean bNewLine = false;   //下一个Token是否输出在新行
//        int nParenCount = 0;        //"(", ")"的匹配计数器
//
//        StringBuffer s = new StringBuffer();
//        List<SqlToken> tokens = new SqlTokenizer().split(sql);
//        for (int i = 0; i < tokens.size(); i ++) {
//            SqlToken curToken = tokens.get(i);
//            String curText = curToken.getText().toUpperCase();
//
//            //prevToken
//            SqlToken prevToken = null;
//            String prevText = null;
//
//            if (i > 0) {
//                prevToken = tokens.get(i-1);
//                prevText = prevToken.getText().toUpperCase();
//            }
//
//            //nextToken
//            SqlToken nextToken = null;
//            String nextText = null;
//
//            if (i < tokens.size()-1) {
//                nextToken = tokens.get(i+1);
//                nextText = nextToken.getText().toUpperCase();
//            }
//
//            if (curToken.isKeyword()) {
//                if (curText.equals("SELECT")) { //select-clause，新增递归层次
//                    nDepth ++;
//                    s.append(NEWLINE).append(pad(nIndent));
//                    s.append(curText);
//                    bNewLine = true;
//                } else if (curText.equals("AS")) {
//                    s.append(" ");
//                    s.append(curText);
//                    s.append(" ");
//                    bNewLine = false;
//                } else if (curText.equals("ON")) {
//                    nIndent ++;
//                    s.append(NEWLINE).append(pad(nIndent));
//                    s.append(curText);
//                    bNewLine = true;
//                } else if (curText.equals("AND") || curText.equals("OR")) {
//                    s.append(NEWLINE).append(pad(nIndent+1));
//                    s.append(curText);
//                    s.append(" ");
//                    bNewLine = false;
//                } else {
//                    nIndent --;
//                    s.append(NEWLINE).append(pad(nDepth+nIndent));
//                    s.append(curText);
//                    nIndent ++;
//                    bNewLine = true;
//                }
//
//
//
//            } else {
//                if (bNewLine) {
//                    s.append(NEWLINE).append(pad(nIndent+1));
//                    bNewLine = false;
//                }
//
//                if (curText.equals("(")) nParenCount ++;
//                else if (curText.equals(")")) nParenCount --;
//
//                if (curToken.getText().equals(",")) {
//                    s.append(curToken.getText());
//                    bNewLine = true;
//                } else {
//                    s.append(curToken.getText());
//                }
//
//            }
//        }
//
//        return s.toString().trim();
//    }
}
