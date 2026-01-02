package top.whitehat.util;

@FunctionalInterface
public interface NotifyEventHandler {
	void onNotify(Object e);
}