/*
 * Copyright (c) 2013-2017, suimi
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author suimi
 * @date 2018/8/27
 */
@ToString
@Getter @Setter public class TestA {
    private TestB b;

    private String name;

    private List<TestB> lists;


}
