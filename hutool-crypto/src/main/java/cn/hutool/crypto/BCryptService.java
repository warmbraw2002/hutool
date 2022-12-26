package cn.hutool.crypto;

public interface BCryptService  {

    //编码
    //默认复杂程度
    String encode(String password);

    //指定复杂程度
    String encode(String password,int rounds);
    //匹配明文和密文
    boolean matches(String password, String encodingPassword);

}
