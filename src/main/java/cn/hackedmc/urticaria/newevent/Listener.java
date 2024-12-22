package cn.hackedmc.urticaria.newevent;

@FunctionalInterface
public interface Listener<Event> {
    void call(Event event);
}