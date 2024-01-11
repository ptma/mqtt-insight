package com.mqttinsight.codec.proto;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.util.*;

import static com.mqttinsight.codec.proto.OptionElement.Kind;
import static com.mqttinsight.codec.proto.OptionElement.of;
import static com.mqttinsight.codec.proto.Syntax.PROTO2;
import static com.mqttinsight.codec.proto.Syntax.PROTO3;

public final class ProtoParser {

    private final String filename;
    private final char[] data;
    private final Proto proto;

    private int pos;
    private int line;
    private int lineStart;
    private String packageName;
    private String prefix = "";

    public ProtoParser(String filename, char[] data) {
        this.filename = filename;
        this.data = data;
        this.proto = new Proto(filename);
    }

    public static Proto parse(String file) {
        return parse(new File(file));
    }

    public static Proto parse(File file) {
        return new ProtoParser(file.getName(), FileUtil.readUtf8String(file).toCharArray()).readProtoFile();
    }

    public Proto readProtoFile() {
        while (true) {
            String documentation = readDocumentation();
            if (pos == data.length) {
                return proto;
            }
            Object declaration = readDeclaration(documentation, Context.FILE);
            if (declaration instanceof TypeElement) {
                proto.addType((TypeElement) declaration);
            } else if (declaration instanceof ServiceElement) {
                proto.addService((ServiceElement) declaration);
            } else if (declaration instanceof OptionElement) {
                proto.addOption((OptionElement) declaration);
            } else if (declaration instanceof ExtendElement) {
                proto.addExtendDeclaration((ExtendElement) declaration);
            }
        }
    }

    private Object readDeclaration(String documentation, Context context) {
        if (peekChar() == ';') {
            pos++;
            return null;
        }

        String label = readWord();

        if ("package".equals(label)) {
            if (!context.permitsPackage()) {
                throw unexpected("'package' in " + context);
            }
            if (packageName != null) {
                throw unexpected("too many package names");
            }
            packageName = readName();
            proto.setPackageName(packageName);
            prefix = packageName + ".";
            if (readChar() != ';') {
                throw unexpected("expected ';'");
            }
            return null;
        } else if ("import".equals(label)) {
            if (!context.permitsImport()) {
                throw unexpected("'import' in " + context);
            }
            String importString = readString();
            if ("public".equals(importString)) {
                proto.addPublicDependency(readString());
            } else {
                proto.addDependency(importString);
            }
            if (readChar() != ';') {
                throw unexpected("expected ';'");
            }
            return null;
        } else if ("syntax".equals(label)) {
            if (!context.permitsSyntax()) {
                throw unexpected("'syntax' in " + context);
            }
            if (readChar() != '=') {
                throw unexpected("expected '='");
            }
            String syntax = readQuotedString();
            switch (syntax) {
                case "proto2" -> proto.setSyntax(PROTO2);
                case "proto3" -> proto.setSyntax(PROTO3);
                default -> throw unexpected("'syntax' must be 'proto2' or 'proto3'. Found: " + syntax);
            }
            if (readChar() != ';') {
                throw unexpected("expected ';'");
            }
            return null;
        } else if ("option".equals(label)) {
            OptionElement result = readOption('=');
            if (readChar() != ';') {
                throw unexpected("expected ';'");
            }
            return result;
        } else if ("message".equals(label)) {
            return readMessage(documentation);
        } else if ("enum".equals(label)) {
            return readEnumElement(documentation);
        } else if ("service".equals(label)) {
            return readService(documentation);
        } else if ("extend".equals(label)) {
            return readExtend(documentation);
        } else if ("rpc".equals(label)) {
            if (!context.permitsRpc()) {
                throw unexpected("'rpc' in " + context);
            }
            return readRpc(documentation);
        } else if ("required".equals(label) || "optional".equals(label) || "repeated".equals(label)) {
            if (!context.permitsField()) {
                throw unexpected("fields must be nested");
            }
            FieldElement.Label labelEnum = FieldElement.Label.valueOf(label.toUpperCase(Locale.US));
            return readField(documentation, labelEnum, null);
        } else if ("oneof".equals(label)) {
            if (!context.permitsOneOf()) {
                throw unexpected("'oneof' must be nested in message");
            }
            return readOneOf(documentation);
        } else if ("extensions".equals(label)) {
            if (!context.permitsExtensions()) {
                throw unexpected("'extensions' must be nested");
            }
            return readExtensions(documentation);
        } else if (context == Context.ENUM) {
            if (readChar() != '=') {
                throw unexpected("expected '='");
            }
            EnumConstantElement enumConstantElement = EnumConstantElement.of(label, readInt(), tryAppendTrailingDocumentation(documentation));
            if (peekChar() == '[') {
                readChar();
                while (true) {
                    enumConstantElement.addOption(readOption('='));
                    char c = readChar();
                    if (c == ']') {
                        break;
                    }
                    if (c != ',') {
                        throw unexpected("Expected ',' or ']");
                    }
                }
            }
            if (readChar() != ';') {
                throw unexpected("expected ';'");
            }
            return enumConstantElement.validate();
        } else if (context == Context.MESSAGE && isDataType(label)) {
            FieldElement.Label labelEnum = FieldElement.Label.OPTIONAL;
            return readField(documentation, labelEnum, label);
        } else {
            throw unexpected("unexpected label: " + label);
        }
    }

    /**
     * Reads a message declaration.
     */
    private MessageElement readMessage(String documentation) {
        String name = readName();
        MessageElement messageElement = MessageElement.of(name, prefix + name, documentation);

        String previousPrefix = prefix;
        prefix = prefix + name + ".";

        if (readChar() != '{') {
            throw unexpected("expected '{'");
        }
        while (true) {
            String nestedDocumentation = readDocumentation();
            if (peekChar() == '}') {
                pos++;
                break;
            }
            Object declared = readDeclaration(nestedDocumentation, Context.MESSAGE);
            if (declared instanceof FieldElement) {
                messageElement.addField((FieldElement) declared);
            } else if (declared instanceof OneOfElement) {
                messageElement.addOneOf((OneOfElement) declared);
            } else if (declared instanceof TypeElement) {
                messageElement.addType((TypeElement) declared);
            } else if (declared instanceof ExtensionsElement) {
                messageElement.addExtensions((ExtensionsElement) declared);
            } else if (declared instanceof OptionElement) {
                messageElement.addOption((OptionElement) declared);
            } else if (declared instanceof ExtendElement) {
                proto.addExtendDeclaration((ExtendElement) declared);
            }
        }
        prefix = previousPrefix;

        return messageElement.validate();
    }

    /**
     * Reads an extend declaration.
     */
    private ExtendElement readExtend(String documentation) {
        String name = readName();
        String qualifiedName = name;
        if (!name.contains(".") && packageName != null) {
            qualifiedName = packageName + "." + name;
        }
        ExtendElement extendElement = ExtendElement.of(name, qualifiedName, documentation);
        if (readChar() != '{') {
            throw unexpected("expected '{'");
        }
        while (true) {
            String nestedDocumentation = readDocumentation();
            if (peekChar() == '}') {
                pos++;
                break;
            }
            Object declared = readDeclaration(nestedDocumentation, Context.EXTEND);
            if (declared instanceof FieldElement) {
                extendElement.addField((FieldElement) declared);
            }
        }
        return extendElement.validate();
    }

    /**
     * Reads a service declaration and returns it.
     */
    private ServiceElement readService(String documentation) {
        String name = readName();
        ServiceElement element = ServiceElement.of(name, prefix + name, documentation);

        if (readChar() != '{') {
            throw unexpected("expected '{'");
        }
        while (true) {
            String rpcDocumentation = readDocumentation();
            if (peekChar() == '}') {
                pos++;
                break;
            }
            Object declared = readDeclaration(rpcDocumentation, Context.SERVICE);
            if (declared instanceof RpcElement) {
                element.addRpc((RpcElement) declared);
            } else if (declared instanceof OptionElement) {
                element.addOption((OptionElement) declared);
            }
        }
        return element.validate();
    }

    private EnumElement readEnumElement(String documentation) {
        String name = readName();
        EnumElement enumElement = EnumElement.of(name, prefix + name, documentation);
        if (readChar() != '{') {
            throw unexpected("expected '{'");
        }
        while (true) {
            String valueDocumentation = readDocumentation();
            if (peekChar() == '}') {
                pos++;
                break;
            }
            Object declared = readDeclaration(valueDocumentation, Context.ENUM);
            if (declared instanceof EnumConstantElement) {
                enumElement.addConstant((EnumConstantElement) declared);
            } else if (declared instanceof OptionElement) {
                enumElement.addOption((OptionElement) declared);
            }
        }
        return enumElement.validate();
    }

    private FieldElement readField(String documentation, FieldElement.Label label, String type) {
        DataType dataType;
        if (type == null) {
            dataType = readDataType();
        } else {
            dataType = toDataType(type);
        }
        String name = readName();
        if (readChar() != '=') {
            throw unexpected("expected '='");
        }
        int tag = readInt();

        FieldElement fieldElement = FieldElement.of(label, dataType, name, tag, tryAppendTrailingDocumentation(documentation));

        if (peekChar() == '[') {
            pos++;
            while (true) {
                fieldElement.addOption(readOption('='));
                char c = peekChar();
                if (c == ']') {
                    pos++;
                    break;
                } else if (c == ',') {
                    pos++;
                }
            }
        }
        if (readChar() != ';') {
            throw unexpected("expected ';'");
        }
        return fieldElement.validate();
    }

    private OneOfElement readOneOf(String documentation) {
        String name = readName();
        OneOfElement oneOfElement = OneOfElement.of(name, documentation);

        if (readChar() != '{') {
            throw unexpected("expected '{'");
        }
        while (true) {
            String nestedDocumentation = readDocumentation();
            if (peekChar() == '}') {
                pos++;
                break;
            }
            oneOfElement.addField(readField(nestedDocumentation, FieldElement.Label.ONE_OF, null));
        }
        return oneOfElement.validate();
    }

    private ExtensionsElement readExtensions(String documentation) {
        int start = readInt();
        int end = start;
        if (peekChar() != ';') {
            if (!"to".equals(readWord())) {
                throw unexpected("expected ';' or 'to'");
            }
            String s = readWord();
            if ("max".equals(s)) {
                end = Proto.MAX_TAG_VALUE;
            } else {
                end = Integer.parseInt(s);
            }
        }
        if (readChar() != ';') {
            throw unexpected("expected ';'");
        }
        return ExtensionsElement.of(start, end, documentation);
    }

    private OptionElement readOption(char keyValueSeparator) {
        boolean isExtension = (peekChar() == '[');
        boolean isParenthesized = (peekChar() == '(');
        String name = readName();
        if (isExtension) {
            name = "[" + name + "]";
        }
        String subName = null;
        char c = readChar();
        if (c == '.') {
            subName = readName();
            c = readChar();
        }
        if (c != keyValueSeparator) {
            throw unexpected("expected '" + keyValueSeparator + "' in option");
        }
        OptionKindAndValue kindAndValue = readKindAndValue();
        Kind kind = kindAndValue.kind();
        Object value = kindAndValue.value();
        if (subName != null) {
            value = of(subName, kind, value);
            kind = Kind.OPTION;
        }
        return of(name, kind, value, isParenthesized);
    }

    public static class OptionKindAndValue {

        private final Kind kind;
        private final Object value;

        public OptionKindAndValue(Kind kind, Object value) {
            this.kind = kind;
            this.value = value;
        }

        static OptionKindAndValue of(Kind kind, Object value) {
            return new OptionKindAndValue(kind, value);
        }

        public Kind kind() {
            return kind;
        }

        public Object value() {
            return value;
        }
    }

    private OptionKindAndValue readKindAndValue() {
        char peeked = peekChar();
        switch (peeked) {
            case '{' -> {
                return OptionKindAndValue.of(Kind.MAP, readMap('{', '}', ':'));
            }
            case '[' -> {
                return OptionKindAndValue.of(Kind.LIST, readList());
            }
            case '"' -> {
                return OptionKindAndValue.of(Kind.STRING, readString());
            }
            default -> {
                if (Character.isDigit(peeked) || peeked == '-') {
                    return OptionKindAndValue.of(Kind.NUMBER, readWord());
                }
                String word = readWord();
                return switch (word) {
                    case "true" -> OptionKindAndValue.of(Kind.BOOLEAN, "true");
                    case "false" -> OptionKindAndValue.of(Kind.BOOLEAN, "false");
                    default -> OptionKindAndValue.of(Kind.ENUM, word);
                };
            }
        }
    }

    private Map<String, Object> readMap(char openBrace, char closeBrace, char keyValueSeparator) {
        if (readChar() != openBrace) {
            throw new AssertionError();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        while (true) {
            if (peekChar() == closeBrace) {
                pos++;
                return result;
            }

            OptionElement option = readOption(keyValueSeparator);
            String name = option.getName();
            Object value = option.getValue();
            if (value instanceof OptionElement) {
                Map<String, Object> nested = (Map<String, Object>) result.get(name);
                if (nested == null) {
                    nested = new LinkedHashMap<>();
                    result.put(name, nested);
                }
                OptionElement valueOption = (OptionElement) value;
                nested.put(valueOption.getName(), valueOption.getValue());
            } else {
                Object previous = result.get(name);
                if (previous == null) {
                    result.put(name, value);
                } else if (previous instanceof List) {
                    addToList((List<Object>) previous, value);
                } else {
                    List<Object> newList = new ArrayList<>();
                    newList.add(previous);
                    addToList(newList, value);
                    result.put(name, newList);
                }
            }

            if (peekChar() == ',') {
                pos++;
            }
        }
    }

    private void addToList(List<Object> list, Object value) {
        if (value instanceof List) {
            list.addAll((List<?>) value);
        } else {
            list.add(value);
        }
    }

    private List<Object> readList() {
        if (readChar() != '[') {
            throw new AssertionError();
        }
        List<Object> result = new ArrayList<>();
        while (true) {
            if (peekChar() == ']') {
                pos++;
                return result;
            }

            result.add(readKindAndValue().value());

            char c = peekChar();
            if (c == ',') {
                pos++;
            } else if (c != ']') {
                throw unexpected("expected ',' or ']'");
            }
        }
    }

    private RpcElement readRpc(String documentation) {
        RpcElement rpcElement = RpcElement.of(readName(), documentation);

        if (readChar() != '(') {
            throw unexpected("expected '('");
        }
        DataType requestType = readDataType();
        if (!(requestType instanceof DataType.NamedType)) {
            throw unexpected("expected message but was " + requestType);
        }
        rpcElement.setRequestType((DataType.NamedType) requestType);
        if (readChar() != ')') {
            throw unexpected("expected ')'");
        }

        if (!"returns".equals(readWord())) {
            throw unexpected("expected 'returns'");
        }

        if (readChar() != '(') {
            throw unexpected("expected '('");
        }
        DataType responseType = readDataType();
        if (!(responseType instanceof DataType.NamedType)) {
            throw unexpected("expected message but was " + responseType);
        }
        rpcElement.setResponseType((DataType.NamedType) responseType);
        if (readChar() != ')') {
            throw unexpected("expected ')'");
        }

        if (peekChar() == '{') {
            pos++;
            while (true) {
                String rpcDocumentation = readDocumentation();
                if (peekChar() == '}') {
                    pos++;
                    break;
                }
                Object declared = readDeclaration(rpcDocumentation, Context.RPC);
                if (declared instanceof OptionElement) {
                    rpcElement.addOption((OptionElement) declared);
                }
            }
        } else if (readChar() != ';') {
            throw unexpected("expected ';'");
        }

        return rpcElement.validate();
    }

    private char readChar() {
        char result = peekChar();
        pos++;
        return result;
    }

    private char peekChar() {
        skipWhitespace(true);
        if (pos == data.length) {
            throw unexpected("unexpected end of file");
        }
        return data[pos];
    }

    private String readString() {
        skipWhitespace(true);
        return peekChar() == '"' ? readQuotedString() : readWord();
    }

    private String readQuotedString() {
        if (readChar() != '"') {
            throw new AssertionError();
        }
        StringBuilder result = new StringBuilder();
        while (pos < data.length) {
            char c = data[pos++];
            if (c == '"') {
                return result.toString();
            }

            if (c == '\\') {
                if (pos == data.length) {
                    throw unexpected("unexpected end of file");
                }
                c = data[pos++];
                switch (c) {
                    case 'a' -> c = 0x7;
                    case 'b' -> c = '\b';
                    case 'f' -> c = '\f';
                    case 'n' -> c = '\n';
                    case 'r' -> c = '\r';
                    case 't' -> c = '\t';
                    case 'v' -> c = 0xb;
                    case 'x', 'X' -> c = readNumericEscape(16, 2);
                    case '0', '1', '2', '3', '4', '5', '6', '7' -> {
                        --pos;
                        c = readNumericEscape(8, 3);
                    }
                    default -> {
                    }
                }
            }

            result.append(c);
            if (c == '\n') {
                newline();
            }
        }
        throw unexpected("unterminated string");
    }

    private char readNumericEscape(int radix, int len) {
        int value = -1;
        for (int endPos = Math.min(pos + len, data.length); pos < endPos; pos++) {
            int digit = hexDigit(data[pos]);
            if (digit == -1 || digit >= radix) {
                break;
            }
            if (value < 0) {
                value = digit;
            } else {
                value = value * radix + digit;
            }
        }
        if (value < 0) {
            throw unexpected("expected a digit after \\x or \\X");
        }
        return (char) value;
    }

    private int hexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else {
            return -1;
        }
    }

    private String readName() {
        String optionName;
        char c = peekChar();
        if (c == '(') {
            pos++;
            optionName = readWord();
            if (readChar() != ')') {
                throw unexpected("expected ')'");
            }
        } else if (c == '[') {
            pos++;
            optionName = readWord();
            if (readChar() != ']') {
                throw unexpected("expected ']'");
            }
        } else {
            optionName = readWord();
        }
        return optionName;
    }

    private DataType readDataType() {
        return toDataType(readWord());
    }

    private DataType toDataType(String name) {
        switch (name) {
            case "map" -> {
                if (readChar() != '<') {
                    throw unexpected("expected '<'");
                }
                DataType keyType = readDataType();
                if (readChar() != ',') {
                    throw unexpected("expected ','");
                }
                DataType valueType = readDataType();
                if (readChar() != '>') {
                    throw unexpected("expected '>'");
                }
                return DataType.MapType.create(keyType, valueType);
            }
            case "any" -> {
                return DataType.ScalarType.ANY;
            }
            case "bool" -> {
                return DataType.ScalarType.BOOL;
            }
            case "bytes" -> {
                return DataType.ScalarType.BYTES;
            }
            case "double" -> {
                return DataType.ScalarType.DOUBLE;
            }
            case "float" -> {
                return DataType.ScalarType.FLOAT;
            }
            case "fixed32" -> {
                return DataType.ScalarType.FIXED32;
            }
            case "fixed64" -> {
                return DataType.ScalarType.FIXED64;
            }
            case "int32" -> {
                return DataType.ScalarType.INT32;
            }
            case "int64" -> {
                return DataType.ScalarType.INT64;
            }
            case "sfixed32" -> {
                return DataType.ScalarType.SFIXED32;
            }
            case "sfixed64" -> {
                return DataType.ScalarType.SFIXED64;
            }
            case "sint32" -> {
                return DataType.ScalarType.SINT32;
            }
            case "sint64" -> {
                return DataType.ScalarType.SINT64;
            }
            case "string" -> {
                return DataType.ScalarType.STRING;
            }
            case "uint32" -> {
                return DataType.ScalarType.UINT32;
            }
            case "uint64" -> {
                return DataType.ScalarType.UINT64;
            }
            default -> {
                return DataType.NamedType.create(name);
            }
        }
    }

    private boolean isDataType(String label) {
        return switch (label) {
            case "map", "any", "bool", "bytes", "double", "float", "fixed32", "fixed64", "int32", "int64", "sfixed32", "sfixed64", "sint32", "sint64", "string", "uint32", "uint64" ->
                true;
            default -> proto.findType(label).isPresent();
        };
    }

    private String readWord() {
        skipWhitespace(true);
        int start = pos;
        while (pos < data.length) {
            char c = data[pos];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_') || (c == '-') || (c == '.')) {
                pos++;
            } else {
                break;
            }
        }
        if (start == pos) {
            throw unexpected("expected a word");
        }
        return new String(data, start, pos - start);
    }

    private int readInt() {
        String tag = readWord();
        try {
            int radix = 10;
            if (tag.startsWith("0x") || tag.startsWith("0X")) {
                tag = tag.substring("0x".length());
                radix = 16;
            }
            return Integer.valueOf(tag, radix);
        } catch (Exception e) {
            throw unexpected("expected an integer but was " + tag);
        }
    }

    private String readDocumentation() {
        String result = null;
        while (true) {
            skipWhitespace(false);
            if (pos == data.length || data[pos] != '/') {
                return result != null ? result : "";
            }
            String comment = readComment();
            result = (result == null) ? comment : (result + "\n" + comment);
        }
    }

    private String readComment() {
        if (pos == data.length || data[pos] != '/') {
            throw new AssertionError();
        }
        pos++;
        int commentType = pos < data.length ? data[pos++] : -1;
        if (commentType == '*') {
            StringBuilder result = new StringBuilder();
            boolean startOfLine = true;

            for (; pos + 1 < data.length; pos++) {
                char c = data[pos];
                if (c == '*' && data[pos + 1] == '/') {
                    pos += 2;
                    return result.toString().trim();
                }
                if (c == '\n') {
                    result.append('\n');
                    newline();
                    startOfLine = true;
                } else if (!startOfLine) {
                    result.append(c);
                } else if (c == '*') {
                    if (data[pos + 1] == ' ') {
                        pos += 1;
                    }
                    startOfLine = false;
                } else if (!Character.isWhitespace(c)) {
                    result.append(c);
                    startOfLine = false;
                }
            }
            throw unexpected("unterminated comment");
        } else if (commentType == '/') {
            if (pos < data.length && data[pos] == ' ') {
                pos += 1;
            }
            int start = pos;
            while (pos < data.length) {
                char c = data[pos++];
                if (c == '\n') {
                    newline();
                    break;
                }
            }
            return new String(data, start, pos - 1 - start);
        } else {
            throw unexpected("unexpected '/'");
        }
    }

    private String tryAppendTrailingDocumentation(String documentation) {
        while (pos < data.length) {
            char c = data[pos];
            if (c == ' ' || c == '\t') {
                pos++;
            } else if (c == '/') {
                pos++;
                break;
            } else {
                return documentation;
            }
        }

        if (pos == data.length || (data[pos] != '/' && data[pos] != '*')) {
            pos--;
            throw unexpected("expected '//' or '/*'");
        }
        boolean isStar = data[pos] == '*';
        pos++;

        if (pos < data.length && data[pos] == ' ') {
            pos++;
        }

        int start = pos;
        int end;

        if (isStar) {
            while (true) {
                if (pos == data.length || data[pos] == '\n') {
                    throw unexpected("trailing comment must be closed on the same line");
                }
                if (data[pos] == '*' && pos + 1 < data.length && data[pos + 1] == '/') {
                    end = pos - 1;
                    pos += 2;
                    break;
                }
                pos++;
            }
            while (pos < data.length) {
                char c = data[pos++];
                if (c == '\n') {
                    newline();
                    break;
                }
                if (c != ' ' && c != '\t') {
                    throw unexpected("no syntax may follow trailing comment");
                }
            }
        } else {
            while (true) {
                if (pos == data.length) {
                    end = pos - 1;
                    break;
                }
                char c = data[pos++];
                if (c == '\n') {
                    newline();
                    end = pos - 2;
                    break;
                }
            }
        }

        while (end > start && (data[end] == ' ' || data[end] == '\t')) {
            end--;
        }

        if (end == start) {
            return documentation;
        }
        String trailingDocumentation = new String(data, start, end - start + 1);
        if (documentation.isEmpty()) {
            return trailingDocumentation;
        }
        return documentation + '\n' + trailingDocumentation;
    }

    private void skipWhitespace(boolean skipComments) {
        while (pos < data.length) {
            char c = data[pos];
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                pos++;
                if (c == '\n') {
                    newline();
                }
            } else if (skipComments && c == '/') {
                readComment();
            } else {
                break;
            }
        }
    }

    private void newline() {
        line++;
        lineStart = pos;
    }

    private int column() {
        return pos - lineStart + 1;
    }

    private int line() {
        return line + 1;
    }

    private ProtoParseException unexpected(String message) {
        throw new ProtoParseException(String.format("Syntax error in %s at %d:%d: %s", filename, line(), column(), message));
    }

    enum Context {
        FILE, MESSAGE, ENUM, RPC, EXTEND, SERVICE;

        public boolean permitsPackage() {
            return this == FILE;
        }

        public boolean permitsSyntax() {
            return this == FILE;
        }

        public boolean permitsImport() {
            return this == FILE;
        }

        public boolean permitsField() {
            return this == MESSAGE || this == EXTEND;
        }

        public boolean permitsExtensions() {
            return this != FILE;
        }

        public boolean permitsRpc() {
            return this == SERVICE;
        }

        public boolean permitsOneOf() {
            return this == MESSAGE;
        }
    }
}
