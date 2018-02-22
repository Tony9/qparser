# QParser

## 开发调试命令
```
QPARSER_HOME=/Users/Tony/Documents/develop/qparser


java -jar /Volumes/USB/tools/antlr/antlr-4.5.3-complete.jar -visitor -package me.tony9.sql.parser -o "$QPARSER_HOME/src/main/java/me/tony9/sql/parser" "$QPARSER_HOME/src/main/antlr/sql/SqlTokenLexer.g4"

java -jar /Volumes/USB/tools/antlr/antlr-4.5.3-complete.jar -visitor -package me.tony9.sql.parser -o "$QPARSER_HOME/src/main/java/me/tony9/sql/parser" "$QPARSER_HOME/src/main/antlr/sql/SqlTokenParser.g4"


mvn install -Dmaven.test.skip=true

```