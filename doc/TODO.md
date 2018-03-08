# QParser

## 开发调试命令
```
QPARSER_HOME=/Users/Tony/Documents/develop/qparser


java -jar /Volumes/USB/tools/antlr/antlr-4.5.3-complete.jar -visitor -package me.tony9.sql.parser -o "$QPARSER_HOME/src/main/java/me/tony9/sql/parser" "$QPARSER_HOME/src/main/antlr/sql/SqlTokenLexer.g4"

java -jar /Volumes/USB/tools/antlr/antlr-4.5.3-complete.jar -visitor -package me.tony9.sql.parser -o "$QPARSER_HOME/src/main/java/me/tony9/sql/parser" "$QPARSER_HOME/src/main/antlr/sql/SqlTokenParser.g4"


mvn install -Dmaven.test.skip=true

```

## TODO

- 存储过程

- 表达式解析
    逻辑表达式
    算术表达式、函数
    特殊表达式: case..when, like, in, between

## Reference
- MySQL 5.7: https://dev.mysql.com/doc/refman/5.7/en/create-table.html