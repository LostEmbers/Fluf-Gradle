package com.google.gson;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class JsonArray extends JsonElement implements Iterable<JsonElement> {
	@Override
	public Iterator<JsonElement> iterator() {
		return null;
	}
	
	@Override
	public void forEach(Consumer<? super JsonElement> action) {
		Iterable.super.forEach(action);
	}
	
	@Override
	public Spliterator<JsonElement> spliterator() {
		return Iterable.super.spliterator();
	}
	
	public JsonElement get(int i) {
		return null;
	}
}
