package edu.uky.cs.nil.tt.io;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A generic adapter is used to configure a {@link com.google.gson.GsonBuilder}
 * to write all subclasses of a given superclass as that superclass.
 * <p>
 * For example, {@link edu.uky.cs.nil.tt.world.World} is a class that may have
 * many subclasses, but when a world is sent to the server's users, it should be
 * sent as a World object, omitting all the details of its specific runtime
 * type. This code configures a GsonBuilder to do so:
 * <pre>
 * {@code
 * GsonBuilder builder = new GsonBuilder();
 * GenericAdapter<World> adapter = new GenericAdapter<>(World.class);
 * builder.registerTypeAdapterFactory(adapter);
 * }</pre>
 * 
 * @param <G> the class of object whose subclasses will always be written as
 * if they were that class
 * @author Stephen G. Ware
 */
public class GenericAdapter<G> implements TypeAdapterFactory {
	
	/**
	 * The class of object whose subclasses will always be written as if they
	 * were this class
	 */
	public final Class<G> type;
	
	/**
	 * Constructs a new generic adapter which will write all subclasses of the
	 * given class as that class.
	 * 
	 * @param type the superclass whose subclasses will all be written as this
	 * class
	 */
	public GenericAdapter(Class<G> type) {
		this.type = type;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		if(!this.type.isAssignableFrom(type.getRawType()))
			return null;
		final TypeAdapter<G> delegate = gson.getDelegateAdapter(this, TypeToken.get(this.type));
		return new TypeAdapter<T>() {
			@Override
			public T read(JsonReader reader) throws IOException {
				return (T) delegate.read(reader);
			}
			@Override
			public void write(JsonWriter writer, T object) throws IOException {
				delegate.write(writer, (G) object);
			}
		};
	}
}