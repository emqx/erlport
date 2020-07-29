package com.erlport.msg;

import com.erlport.erlang.term.Atom;

/**
 * @author wangwenhai
 * @date 2020/7/14
 * File description: Just a tip message
 */
public class PureMessage extends Message{
    private Integer id;
    private Object result;

    public PureMessage() {
        setType(new Atom("'M'"));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "PureMessage{" +
                "id=" + id +
                ", result=" + result +
                '}';
    }
}
