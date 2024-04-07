package edu.fdu.se.core.miningchangeentity.base;

import java.util.Objects;

/**
 * 标准化name，具体标准格式见类内注释
 */
public class CanonicalName {

    private String prefixName;

    private String selfName;

    private String longName;

    /**
     * 1. ClassChange ClassName
     * 2. MethodChange ClassName.method(int,int)
     * 3. FieldChange ClassName.field
     * 4. Initializer ClassName.{}
     * 5. InnerClass ClassName$InnerClassName
     * 6. InnerClassMethod ClassName$InnerClassName.method(int,int)
     * 7. StmtChange ClassName.method(int,int)@Stmt
     * 8. AnonymousClassInField ClassName.field.
     * <p>
     * e.g. PoolTask.init(Fact,Handler)@Statement#Anonymous.exec(Runnable)@Statement
     */

    /**
     * 0. ChangeEntity Type                   prefixName                          selfName
     * 1. ClassChange                         "^"                                  ClassName
     * 2. MethodChange                        ^ClassName.                          method(int,int)
     * 3. FieldChange                         ^ClassName.                          fieldName
     * 4. Initializer                         ^ClassName.                          {}
     * 5. InnerClassChange                    ^ClassName$                          InnerClassName
     * 6. InnerClassMethodChange              ^ClassName$InnerClassName.           method(int,int)
     * 7. StmtChange                          ^ClassName.method(int,int)@          Stmt
     *
     * 8. AnonymousClassInField ClassName.field.
     * <p>
     * e.g. PoolTask.init(Fact,Handler)@Statement#Anonymous.exec(Runnable)@Statement
     */
    public CanonicalName(String prefix, String selfName){
        this.prefixName = prefix;
        this.selfName = selfName;
        this.longName = this.prefixName + this.selfName;
        //myflag : BUG HERE,prefixName may contain selfName
        //构造函数？
        if(Objects.equals(prefixName, "^"+selfName)){
            this.longName = prefixName;
        }
    }

    public String getLongName(){
        return longName;
    }

    public String getPrintName(){
        return longName.substring(1);
    }

    public String getPrefixName() {
        return prefixName;
    }


    public String getSelfName() {
        return selfName;
    }

    public void setSelfName(String selfName) {
        this.selfName = selfName;
    }

    public void setLongName(String s) {
        this.longName = s;
    }
}
