package hidef;

public interface Auth0TokenHelper<T> {
    String generateToken(T var1, int var2);

    T decodeToken(String var1);
}
