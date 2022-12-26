package cn.hutool.crypto;

public class BCryptIML implements BCryptService {
    @Override
    public String encode(String password) {
        //默认复杂程度为10
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());//密文
        return hashed;
    }

    @Override
    public String encode(String password, int rounds) {
        // 使用hashpw加密密码,得到hash密文
        // gensalt函数的参数大小决定了密文的复杂程度，参数越大，复杂程度越高
        // gensalt函数默认复杂程度为10，最大不超过30.
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(rounds));
        return hashed;
    }

    @Override
    public boolean matches(String password, String encodingPassword) {
        return BCrypt.checkpw(password, encodingPassword);
    }


}
