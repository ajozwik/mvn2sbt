package pl.jozwik.serialization;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;

import pl.jozwik.gen.Entity;
import pl.jozwik.xsdgenxjb.IntervalType;

public class SerializationUtils {

	
	private  final TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
	private final TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());

	public byte[] serialize(Entity entity) throws TException {
		return serializer.serialize(entity);
	}

	public Entity deserialize(byte[] bytes) throws TException {
        new IntervalType();
        Entity entity = new Entity();
		deserializer.deserialize(entity, bytes);
		return entity;
	}

}
