package com.hty.forum.myenum;

/**
 * create by Semineces on 2020-10-05
 * 发帖所属的类型
 */
public enum  QuestionCategory {

    PUT_QUESTIONS(1,"提问"),
    SHARE(2,"分享"),
    DISCUSS(3,"讨论"),
    ADVISE(4,"建议"),
    BUG(5,"Bug"),
    FOR_JOB(6,"工作"),
    NOTICE(7,"公告"),
    TEACH(8,"教程"),
    INTERVIEW(9,"面试");

    private Integer value;
    private String name;

    QuestionCategory(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameByVal(Integer value) {  //通过值去找到名字
        QuestionCategory[] values = QuestionCategory.values();
        for (Integer i = 0; i < value; i++) {
            if (values[i].getValue().equals(value)) {
                return values[i].name;
            }
        }
        return "";
    }
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
