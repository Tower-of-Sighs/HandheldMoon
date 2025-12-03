package com.sighs.handheldmoon.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

public interface InputEvent {


    @FunctionalInterface
    interface MouseButtonPre {
        /**
         * @return true 表示取消
         */
        boolean onMouseButtonPre(MouseButtonInfo mouseButtonInfo, int action);
    }

    @FunctionalInterface
    interface MouseButtonPost {
        void onMouseButtonPost(MouseButtonInfo mouseButtonInfo, int action);
    }

    Event<MouseButtonPre> MOUSE_BUTTON_PRE = EventFactory.createArrayBacked(MouseButtonPre.class,
            callbacks -> (mouseButtonInfo, action) -> {
                for (MouseButtonPre cb : callbacks) {
                    if (cb.onMouseButtonPre(mouseButtonInfo, action)) {
                        return true; // 任何一个返回 true 视为取消
                    }
                }
                return false;
            });

    Event<MouseButtonPost> MOUSE_BUTTON_POST = EventFactory.createArrayBacked(MouseButtonPost.class,
            callbacks -> (mouseButtonInfo, action) -> {
                for (MouseButtonPost cb : callbacks) {
                    cb.onMouseButtonPost(mouseButtonInfo, action);
                }
            });


    @FunctionalInterface
    interface MouseScroll {
        /**
         * @return true 表示取消
         */
        boolean onMouseScroll(double scrollDeltaX, double scrollDeltaY, double mouseX, double mouseY,
                              boolean leftDown, boolean middleDown, boolean rightDown);
    }

    Event<MouseScroll> MOUSE_SCROLL = EventFactory.createArrayBacked(MouseScroll.class,
            callbacks -> (scrollDeltaX, scrollDeltaY, mouseX, mouseY, leftDown, middleDown, rightDown) -> {
                for (MouseScroll cb : callbacks) {
                    if (cb.onMouseScroll(scrollDeltaX, scrollDeltaY, mouseX, mouseY, leftDown, middleDown, rightDown)) {
                        return true;
                    }
                }
                return false;
            });


    @FunctionalInterface
    interface Key {
        void onKey(KeyEvent keyEvent);
    }

    Event<Key> KEY = EventFactory.createArrayBacked(Key.class,
            callbacks -> (key) -> {
                for (Key cb : callbacks) {
                    cb.onKey(key);
                }
            });
}