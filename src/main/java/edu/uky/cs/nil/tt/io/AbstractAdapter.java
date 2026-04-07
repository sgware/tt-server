package edu.uky.cs.nil.tt.io;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * An abstract adapter is used to configure a {@link
 * com.google.gson.GsonBuilder} to read and write objects that share a common
 * parent class, including abstract types like interfaces and abstract classes.
 * When an object of this adapter's type is {@link Gson#toJson(JsonElement)
 * written to JSON}, a {@link #TYPE_NAME key} is added to annotate the object
 * with its runtime type. When that object is later {@link
 * Gson#fromJson(JsonElement, Class) read from JSON} as the parent type, this
 * type information is used to create the correct kind of object.
 * <p>
 * For example, {@link Message} is an abstract class, so while a Message can be
 * written to JSON, it cannot be read as class Message, since that class cannot
 * be instantiated. An abstract adapter can be used like this:
 * <pre>
 * {@code
 * GsonBuilder builder = new GsonBuilder();
 * AbstractAdapter<Message> adapter = new AbstractAdapter<>(Message.class);
 * builder.registerTypeAdapterFactory(adapter);
 * builder.registerTypeAdapter(Message.class, adapter);
 * }</pre>
 * When a Message object is written to JSON, it will now include an additional
 * key that gives its specific runtime type (i.e. which type of Message it is).
 * This makes it possible to read the object from JSON as class Message, since
 * the adapter will now know which specific type of Message object to create.
 * 
 * @param <A> the abstract type this adapter will handle
 * @author Stephen G. Ware
 */
public class AbstractAdapter<A> implements TypeAdapterFactory, JsonDeserializer<A> {
	
	/**
	 * The name of the JSON key whose value will be the object's runtime type
	 */
	public static final String TYPE_NAME = "type";
	
	/** The abstract type this adapter handles */
	private final Class<A> type;
	
	/**
	 * Constructs a new abstract adapter for the given abstract type.
	 * 
	 * @param type an abstract type
	 */
	public AbstractAdapter(Class<A> type) {
		this.type = type;
	}
	
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		if(!this.type.isAssignableFrom(type.getRawType()))
			return null;
		final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
		return new TypeAdapter<T>() {
			@Override
			public T read(JsonReader reader) throws IOException {
				return delegate.read(reader);
			}
			@Override
			public void write(JsonWriter writer, T object) throws IOException {
				JsonElement element = delegate.toJsonTree(object);
				if(element.isJsonObject())
					element.getAsJsonObject().addProperty(TYPE_NAME, object.getClass().getSimpleName());
				Streams.write(element, writer);
			}
		};
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public A deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		String name = this.type.getPackageName() + "." + object.get(TYPE_NAME).getAsString();
		try {
			Class<? extends A> subtype = (Class<? extends A>) Class.forName(name);
			if(this.type.isAssignableFrom(subtype))
				return context.deserialize(element, subtype);
			throw new ClassNotFoundException(name);
		}
		catch (ClassNotFoundException exception) {
			throw new JsonParseException(exception);
		}
	}
}