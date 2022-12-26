package cn.hutool.crypto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BCryptServiceTest {
    BCryptService bCryptService=new BCryptIML();
    //测试用例集合，第一列是明文，第二列是salt，第三列是根据明文和salt得出的密文
    String test_vectors[][] = {
            {        "",
                    "$2a$06$DCq7YPn5Rq63x1Lad4cll.",
                    "$2a$06$DCq7YPn5Rq63x1Lad4cll.TV4S6ytwfsfvkgY8jIucDrjc8deX1s." },
            {        "a",
                    "$2a$06$m0CrhHm10qJ3lXRY.5zDGO",
                    "$2a$06$m0CrhHm10qJ3lXRY.5zDGO3rS2KdeeWLuGmsfGlMfOxih58VYVfxe" },
            {       "abcdefghijklmnopqrstuvwxyz",
                    "$2a$06$.rCVZVOThsIa97pEDOxvGu",
                    "$2a$06$.rCVZVOThsIa97pEDOxvGuRRgzG64bvtJ0938xuqzv18d3ZpQhstC" },

            {        "~!@#$%^&*()      ~!@#$%^&*()PNBFRD",
                    "$2a$06$fPIsBO8qRqkjj273rfaOI.",
                    "$2a$06$fPIsBO8qRqkjj273rfaOI.HtSV9jLDpTbZn782DC6/t7qT67P6FfO" },

    };

    /**
     * 测试方法BCrypt.hashpw(String, String)
     * 即测试加密算法
     */
    @Test
    public void testHashpw() {
        System.out.println("加密测试开始：");
        for (int i = 0; i < test_vectors.length; i++) {
            String plain = test_vectors[i][0];
            String salt = test_vectors[i][1];
            String expected = test_vectors[i][2];
            String hashed = BCrypt.hashpw(plain, salt);
            assertEquals(hashed, expected);
            System.out.println("加密测试"+(i+1)+"成功");
            System.out.println("明文为："+plain);
            System.out.println("对应密文为："+expected);
        }
    }

    /**
     * 测试匹配算法，即密文和明文是否能匹配
     */
    @Test
    public void testCheck() {
        System.out.println("匹配测试开始：");
        for (int i = 0; i < test_vectors.length; i++) {
            String plain = test_vectors[i][0];
            String expected = test_vectors[i][2];
            assertTrue(bCryptService.matches(plain,expected));
            System.out.println("匹配测试"+(i+1)+"成功");
            System.out.println("明文为："+plain);
            System.out.println("对应密文为："+expected);
        }
        System.out.println("");
    }

}
