package bolivariano.arbitro;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

public class BodyProcessor implements Processor {
	static final Logger logger = Logger.getLogger(BodyProcessor.class);

	public void process(Exchange exchange) throws Exception {
		Jedis jedis = null;
		CamelContext context = exchange.getContext();
		exchange.getIn().setHeader("offlineAllow", true);
		String modoArbitro = "";
		try {
			String redisServer = context.resolvePropertyPlaceholders("{{redis.server}}");
			logger.log(Level.DEBUG, "REDIS SELECTED-->" + redisServer);		
			jedis = new Jedis(redisServer);
			String modoArbitroKeyName = context.resolvePropertyPlaceholders("{{redis.keyNameModoArbitro}}");
			modoArbitro = jedis.get(modoArbitroKeyName);
			logger.log(Level.DEBUG, "MODO SELECTED-->" + modoArbitro);

			String arbitroAllow = jedis.get("arbitroAllow");
			exchange.getIn().setHeader("arbitroAllow", arbitroAllow);
			
			if(modoArbitro.equalsIgnoreCase("offline")) {
				
				String offlinekeyAllow = context.resolvePropertyPlaceholders("{{redis.offlinekeyAllow}}")+exchange.getIn().getHeaders().get("operationNamespace");
				String[] key= {offlinekeyAllow};				
				List<String> transaccionesPermitidas = jedis.mget(key);		
				String nombreOperacion=String.valueOf(exchange.getIn().getHeaders().get("operationName"));
				if(!transaccionesPermitidas.contains(nombreOperacion)) {
					
					exchange.getIn().setHeader("offlineAllow", false);
				}
				
				
				
			}
			Iterator<Map.Entry<String, Object>> it = exchange.getIn().getHeaders().entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<String, Object> pair = it.next();			   
			    logger.log(Level.DEBUG, "HEADER-->"  + pair.getKey() +":"+ pair.getValue());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.ERROR, "ERROR AL OBTENER LA CONEXION JEDIS-->" + e.getMessage());
			String modoArbitroDefault = context.resolvePropertyPlaceholders("{{redis.modoDefaultArbitro}}");
			logger.log(Level.ERROR, "OBTENIENDO MODO ARBITRO DEFAULT DE PROPERTIES-->" + e.getMessage());
			modoArbitro = modoArbitroDefault;
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		String payload = exchange.getIn().getBody(String.class);
		String envelope = context.resolvePropertyPlaceholders("{{common.envelope}}");		
		payload = envelope.replace("{Body}", payload);
		exchange.getIn().setBody(payload);
		exchange.getIn().setHeader("modoArbitro", modoArbitro);
	

	}

}
