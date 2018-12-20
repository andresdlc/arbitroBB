package bolivariano.arbitro;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Callback handler to handle passwords
 */
public class UTPasswordCallback implements CallbackHandler {

    private Map<String, String> passwords = new HashMap<String, String>();
    private byte[] secret;
    
  /*  @Value("${seguridad.clave1}")
	private String seguridadclave1;
    @Value("$seguridad.valor1}")
	private String seguridadvalor1;
    @Value("${seguridad.clave2}")
	private String seguridadclave2;
    @Value("${seguridad.valor2}")
	private String seguridadvalor2;*/
    
    public UTPasswordCallback() {        
        Properties prop = new Properties();
        try {
            prop.load(UTPasswordCallback.class.getClassLoader().getResourceAsStream("application.properties"));
            
            
            passwords.put(prop.getProperty("encryption.clave1"), prop.getProperty("encryption.valor1"));
            passwords.put(prop.getProperty("encryption.clave2"), prop.getProperty("encryption.valor2"));           
        } 
        catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    /**
     * Here, we attempt to get the password from the private alias/passwords map.
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        String user = "";

        for (Callback callback : callbacks) {
            String pass = passwords.get(getIdentifier(callback));
            if (pass != null) {
                setPassword(callback, pass);
                return;
            }
            
        }

        // Password not found
        throw new IOException("Password does not exist for the user : " + user);
    }
    
    private void setPassword(Callback callback, String pass) {
        try {
            callback.getClass().getMethod("setPassword", String.class).invoke(callback, pass);
            callback.getClass().getMethod("setKey", byte[].class).invoke(callback, secret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unused")
	private String getPassword(Callback callback) {
        try {
            return (String)callback.getClass().getMethod("getPassword").invoke(callback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getIdentifier(Callback cb) {
        try {
            return (String)cb.getClass().getMethod("getIdentifier").invoke(cb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add an alias/password pair to the callback mechanism.
     */
    public void setAliasPassword(String alias, String password) {
        passwords.put(alias, password);
    }
    
    @SuppressWarnings("unused")
	private String getPasswordType(Callback pc) {
        try {
            Method getType = null;
            try {
                getType = pc.getClass().getMethod("getPasswordType");
            } catch (NoSuchMethodException ex) {
                // keep looking 
            } catch (SecurityException ex) {
                // keep looking
            }
            if (getType == null) {
                getType = pc.getClass().getMethod("getType");
            }
            String result = (String)getType.invoke(pc);
            return result;
            
        } catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Set the Key.
     * <p/>
     *
     * @param secret
     */
    public void setKey(byte[] secret) {
        this.secret = secret;
    }
    public byte[] getKey() {
    	
    	
        return this.secret;
    }
}