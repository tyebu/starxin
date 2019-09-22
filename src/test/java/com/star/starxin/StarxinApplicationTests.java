package com.star.starxin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StarxinApplicationTests {
    @Autowired
    private Sid sid;
    @Test
    public void contextLoads() {
        String s = sid.nextShort();
        System.out.println(s);
    }

}
