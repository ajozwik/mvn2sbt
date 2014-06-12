package pl.jozwik.serialization;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;

import pl.jozwik.gen.Entity;
import org.testng.annotations.Test;

public class SerializationUtilsTest {

	
	@Test
	public void serialize() throws TException {
        Entity entity = new Entity();
		new SerializationUtils().serialize(entity);
	}

}
