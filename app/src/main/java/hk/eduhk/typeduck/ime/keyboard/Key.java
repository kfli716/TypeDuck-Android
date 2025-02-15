/*
 * Copyright (C) 2015-present, osfans
 * waxaca@163.com https://github.com/osfans
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hk.eduhk.typeduck.ime.keyboard;

import static android.view.KeyEvent.isModifierKey;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import hk.eduhk.typeduck.core.Rime;
import hk.eduhk.typeduck.data.AppPrefs;
import hk.eduhk.typeduck.data.theme.Config;
import hk.eduhk.typeduck.ime.enums.KeyEventType;
import hk.eduhk.typeduck.util.CollectionUtils;
import hk.eduhk.typeduck.util.ConfigGetter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import hk.eduhk.typeduck.util.DimensionsKt;
import timber.log.Timber;

/** {@link Keyboard 鍵盤}中的各個按鍵，包含單擊、長按、滑動等多種{@link Event 事件} */
public class Key {
  public static final int[] KEY_STATE_NORMAL_ON = {
    android.R.attr.state_checkable, android.R.attr.state_checked
  };
  public static final int[] KEY_STATE_PRESSED_ON = {
    android.R.attr.state_pressed, android.R.attr.state_checkable, android.R.attr.state_checked
  };
  public static final int[] KEY_STATE_NORMAL_OFF = {android.R.attr.state_checkable};
  public static final int[] KEY_STATE_PRESSED_OFF = {
    android.R.attr.state_pressed, android.R.attr.state_checkable
  };
  public static final int[] KEY_STATE_NORMAL = {};
  public static final int[] KEY_STATE_PRESSED = {android.R.attr.state_pressed};
  public static final int[][] KEY_STATES =
      new int[][] {
        KEY_STATE_PRESSED_ON, // 0    "hilited_on_key_back_color"   锁定时按下的背景
        KEY_STATE_PRESSED_OFF, // 1   "hilited_off_key_back_color"  功能键按下的背景
        KEY_STATE_NORMAL_ON, // 2     "on_key_back_color"           锁定时背景
        KEY_STATE_NORMAL_OFF, // 3    "off_key_back_color"          功能键背景
        KEY_STATE_PRESSED, // 4       "hilited_key_back_color"      按键按下的背景
        KEY_STATE_NORMAL // 5         "key_back_color"              按键背景
      };

  public static Map<String, Map<String, Object>> presetKeys;
  private static final int EVENT_NUM = KeyEventType.values().length;
  public Event[] events = new Event[EVENT_NUM];
  public int edgeFlags;
  private static final KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
  private final Keyboard mKeyboard;

  private boolean send_bindings = true;
  private int width;
  private int height;
  private int gap;
  private int row;
  private int column;
  private String label, cangjieLabel;
  private String hint, hasMenuHint;
  private Drawable key_back_color;
  private Drawable hilited_key_back_color;
  private Integer key_text_color;
  private Integer key_symbol_color;
  private Integer hilited_key_text_color;
  private Integer hilited_key_symbol_color;
  private Integer key_text_size;
  private Integer symbol_text_size;
  private Float round_corner;
  private int key_text_offset_x;
  private int key_text_offset_y;
  private int key_symbol_offset_x;
  private int key_symbol_offset_y;
  private int key_hint_offset_x;
  private int key_hint_offset_y;
  private int key_press_offset_x;
  private int key_press_offset_y;
  private int x;
  private int y;
  private boolean pressed;
  private boolean on;
  private String popupCharacters;
  private int popupResId;
  private String labelSymbol;

  /**
   * Create an empty key with no attributes.
   *
   * @param parent 按鍵所在的{@link Keyboard 鍵盤}
   */
  public Key(Keyboard parent) {
    mKeyboard = parent;
  }
  /**
   * Create an empty key with no attributes.
   *
   * @param parent 按鍵所在的{@link Keyboard 鍵盤}
   * @param mk 從YAML中解析得到的Map
   */
  public Key(Keyboard parent, Map<String, Object> mk) {
    this(parent);
    String s;
    Config config = Config.get();
    {
      boolean hasComposingKey = false;

      for (final KeyEventType type : KeyEventType.values()) {
        final String typeStr = type.toString().toLowerCase(Locale.ROOT);
        s = CollectionUtils.obtainString(mk, typeStr, "");
        if (!TextUtils.isEmpty(s)) {
          events[type.ordinal()] = new Event(mKeyboard, s);
          hasComposingKey = type.ordinal() < KeyEventType.COMBO.ordinal();
        } else if (type == KeyEventType.CLICK) {
          events[type.ordinal()] = new Event(mKeyboard, "");
        }
      }
      if (hasComposingKey) mKeyboard.getComposingKeys().add(this);

      label = ConfigGetter.getString(mk, "label", "");
      cangjieLabel = ConfigGetter.getString(mk, "cangjie_label", "");
      labelSymbol = ConfigGetter.getString(mk, "label_symbol", "");
      hint = ConfigGetter.getString(mk, "hint", "");
      hasMenuHint = ConfigGetter.getString(mk, "has_menu_hint", "");
      if (mk.containsKey("send_bindings")) {
        send_bindings = ConfigGetter.getBoolean(mk, "send_bindings", true);
      } else if (!hasComposingKey) {
        send_bindings = false;
      }
    }

    mKeyboard.setModiferKey(getCode(), this);
    key_text_size = (int) DimensionsKt.sp2px(ConfigGetter.getFloat(mk, "key_text_size", 0));
    symbol_text_size = (int) DimensionsKt.sp2px(ConfigGetter.getFloat(mk, "symbol_text_size", 0));
    key_text_color = config.colors.getColor(mk, "key_text_color");
    hilited_key_text_color = config.colors.getColor(mk, "hilited_key_text_color");
    key_back_color = config.colors.getDrawable(mk, "key_back_color");
    hilited_key_back_color = config.colors.getDrawable(mk, "hilited_key_back_color");
    key_symbol_color = config.colors.getColor(mk, "key_symbol_color");
    hilited_key_symbol_color = config.colors.getColor(mk, "hilited_key_symbol_color");
    round_corner = ConfigGetter.getFloat(mk, "round_corner", 0) * Keyboard.adjustRatio;
  }

  public static Map<String, Map<String, Object>> getPresetKeys() {
    return presetKeys;
  }

  public static KeyCharacterMap getKcm() {
    return kcm;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getGap() {
    return gap;
  }

  public void setGap(int gap) {
    this.gap = gap;
  }

  public int getEdgeFlags() {
    return edgeFlags;
  }

  public void setEdgeFlags(int edgeFlags) {
    this.edgeFlags = edgeFlags;
  }

  public int getRow() {
    return row;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public String getHint() {
    if (!TextUtils.isEmpty(cangjieLabel) && Rime.schemaIsCangjie())
      return getClick().getLabel();
    if (!TextUtils.isEmpty(hasMenuHint) && Rime.hasMenu() && Rime.schemaIsCantonese())
      return hasMenuHint;
    return hint;
  }

  public Integer getKey_text_size() {
    return key_text_size;
  }

  public Integer getSymbol_text_size() {
    return symbol_text_size;
  }

  public Float getRound_corner() {
    return round_corner;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean isPressed() {
    return pressed;
  }

  public boolean isOn() {
    return on;
  }

  public boolean setOn(boolean on) {
    if (on && this.on) this.on = false;
    else this.on = on;
    return this.on;
  }

  public String getPopupCharacters() {
    return popupCharacters;
  }

  public int getPopupResId() {
    return popupResId;
  }

  public int getKey_text_offset_x() {
    return key_text_offset_x + getKey_offset_x();
  }

  public void setKey_text_offset_x(int key_text_offset_x) {
    this.key_text_offset_x = key_text_offset_x;
  }

  public int getKey_text_offset_y() {
    return key_text_offset_y + getKey_offset_y();
  }

  public void setKey_text_offset_y(int key_text_offset_y) {
    this.key_text_offset_y = key_text_offset_y;
  }

  public int getKey_symbol_offset_x() {
    return key_symbol_offset_x + getKey_offset_x();
  }

  public void setKey_symbol_offset_x(int key_symbol_offset_x) {
    this.key_symbol_offset_x = key_symbol_offset_x;
  }

  public int getKey_symbol_offset_y() {
    return key_symbol_offset_y + getKey_offset_y();
  }

  public void setKey_symbol_offset_y(int key_symbol_offset_y) {
    this.key_symbol_offset_y = key_symbol_offset_y;
  }

  public int getKey_hint_offset_x() {
    return key_hint_offset_x + getKey_offset_x();
  }

  public void setKey_hint_offset_x(int key_hint_offset_x) {
    this.key_hint_offset_x = key_hint_offset_x;
  }

  public int getKey_hint_offset_y() {
    return key_hint_offset_y + getKey_offset_y();
  }

  public void setKey_hint_offset_y(int key_hint_offset_y) {
    this.key_hint_offset_y = key_hint_offset_y;
  }

  public void setKey_press_offset_x(int key_press_offset_x) {
    this.key_press_offset_x = key_press_offset_x;
  }

  public void setKey_press_offset_y(int key_press_offset_y) {
    this.key_press_offset_y = key_press_offset_y;
  }

  public int getKey_offset_x() {
    return pressed ? key_press_offset_x : 0;
  }

  public int getKey_offset_y() {
    return pressed ? key_press_offset_y : 0;
  }

  private boolean isNormal(int[] drawableState) {
    return (drawableState == KEY_STATE_NORMAL || drawableState == KEY_STATE_NORMAL_OFF);
  }

  public Drawable getBackColorForState(int[] drawableState) {
    if (isNormal(drawableState)) return key_back_color;
    else return hilited_key_back_color;
  }

  public Integer getTextColorForState(int[] drawableState) {
    if (isNormal(drawableState)) return key_text_color;
    else return hilited_key_text_color;
  }

  public Integer getSymbolColorForState(int[] drawableState) {
    if (isNormal(drawableState)) return key_symbol_color;
    else return hilited_key_symbol_color;
  }

  /**
   * Informs the key that it has been pressed, in case it needs to change its appearance or state.
   *
   * @see #onReleased(boolean)
   */
  public void onPressed() {
    pressed = !pressed;
  }

  /**
   * Changes the pressed state of the key. If it is a sticky key, it will also change the toggled
   * state of the key if the finger was release inside.
   *
   * @param inside whether the finger was released inside the key
   * @see #onPressed()
   */
  public void onReleased(boolean inside) {
    pressed = !pressed;
    if (getClick().isSticky()) on = !on;
  }

  /**
   * Detects if a point falls inside this key.
   *
   * @param x the x-coordinate of the point
   * @param y the y-coordinate of the point
   * @return whether or not the point falls inside the key. If the key is attached to an edge, it
   *     will assume that all points between the key and the edge are considered to be inside the
   *     key.
   */
  public boolean isInside(int x, int y) {
    final boolean leftEdge = (edgeFlags & Keyboard.EDGE_LEFT) > 0;
    final boolean rightEdge = (edgeFlags & Keyboard.EDGE_RIGHT) > 0;
    final boolean topEdge = (edgeFlags & Keyboard.EDGE_TOP) > 0;
    final boolean bottomEdge = (edgeFlags & Keyboard.EDGE_BOTTOM) > 0;
    return (x >= this.x || (leftEdge && x <= this.x + this.width))
        && (x < this.x + this.width || (rightEdge && x >= this.x))
        && (y >= this.y || (topEdge && y <= this.y + this.height))
        && (y < this.y + this.height || (bottomEdge && y >= this.y));
  }

  /**
   * Returns the square of the distance between the center of the key and the given point.
   *
   * @param x the x-coordinate of the point
   * @param y the y-coordinate of the point
   * @return the square of the distance of the point from the center of the key
   */
  public int squaredDistanceFrom(int x, int y) {
    int xDist = this.x + width / 2 - x;
    int yDist = this.y + height / 2 - y;
    return xDist * xDist + yDist * yDist;
  }

  // Trime把function键消费掉了，因此键盘只处理function键以外的修饰键
  public boolean isTrimeModifierKey() {
    return isTrimeModifierKey(getCode());
  }

  public static boolean isTrimeModifierKey(int keycode) {
    if (keycode == KeyEvent.KEYCODE_FUNCTION) return false;
    return isModifierKey(keycode);
  }

  public void printModifierKeyState(String invalidKey) {
    if (isTrimeModifierKey())
      Timber.d(
          "\t<TrimeInput>\tkeyState() key=%s, isShifted=%s, on=%s, invalidKey=%s",
          getLabel(), mKeyboard.hasModifier(getModifierKeyOnMask()), on, invalidKey);
  }

  public void printModifierKeyState() {
    if (isTrimeModifierKey())
      Timber.d(
          "\t<TrimeInput>\tkeyState() key=%s, isShifted=%s, on=%s",
          getLabel(), mKeyboard.hasModifier(getModifierKeyOnMask()), on);
  }

  /**
   * Returns the drawable state for the key, based on the current state and type of the key.
   *
   * @return the drawable state of the key.
   * @see android.graphics.drawable.StateListDrawable#setState(int[])
   */
  public int[] getCurrentDrawableState() {
    int[] states = KEY_STATE_NORMAL;
    boolean isShifted = isTrimeModifierKey() && mKeyboard.hasModifier(getModifierKeyOnMask());

    if (isShifted || on) {
      if (pressed) {
        states = KEY_STATE_PRESSED_ON;
      } else {
        states = KEY_STATE_NORMAL_ON;
      }
    } else {
      if (getClick().isSticky() || getClick().isFunctional()) {
        if (pressed) {
          states = KEY_STATE_PRESSED_OFF;
        } else {
          states = KEY_STATE_NORMAL_OFF;
        }
      } else {
        if (pressed) {
          states = KEY_STATE_PRESSED;
        }
      }
    }

    // only for modiferKey debug
    if (isTrimeModifierKey())
      mKeyboard.printModifierKeyState(
          MessageFormat.format(
              "getCurrentDrawableState() Key={0} states={1} on={2} isShifted={3} pressed={4} sticky={5}",
              getLabel(),
              Arrays.asList(KEY_STATES).indexOf(states),
              on,
              isShifted,
              pressed,
              getClick().isSticky()));
    return states;
  }

  public int getModifierKeyOnMask() {
    return getModifierKeyOnMask(getCode());
  }

  public int getModifierKeyOnMask(int keycode) {
    if (keycode == KeyEvent.KEYCODE_SHIFT_LEFT || keycode == KeyEvent.KEYCODE_SHIFT_RIGHT)
      return KeyEvent.META_SHIFT_ON;
    if (keycode == KeyEvent.KEYCODE_CTRL_LEFT || keycode == KeyEvent.KEYCODE_CTRL_RIGHT)
      return KeyEvent.META_CTRL_ON;
    if (keycode == KeyEvent.KEYCODE_META_LEFT || keycode == KeyEvent.KEYCODE_META_RIGHT)
      return KeyEvent.META_META_ON;
    if (keycode == KeyEvent.KEYCODE_ALT_LEFT || keycode == KeyEvent.KEYCODE_ALT_RIGHT)
      return KeyEvent.META_ALT_ON;
    if (keycode == KeyEvent.KEYCODE_SYM) return KeyEvent.META_SYM_ON;
    return 0;
  }

  public boolean isShift() {
    int c = getCode();
    return (c == KeyEvent.KEYCODE_SHIFT_LEFT || c == KeyEvent.KEYCODE_SHIFT_RIGHT);
  }

  public boolean isCtrl() {
    int c = getCode();
    return (c == KeyEvent.KEYCODE_CTRL_LEFT || c == KeyEvent.KEYCODE_CTRL_RIGHT);
  }

  public boolean isMeta() {
    int c = getCode();
    return (c == KeyEvent.KEYCODE_META_LEFT || c == KeyEvent.KEYCODE_META_RIGHT);
  }

  public boolean isAlt() {
    int c = getCode();
    return (c == KeyEvent.KEYCODE_ALT_LEFT || c == KeyEvent.KEYCODE_ALT_RIGHT);
  }

  public boolean isSpace() {
    int c = getCode();
    return (c == KeyEvent.KEYCODE_SPACE);
  }

  public boolean isSys() {
    int c = getCode();
    return (c == KeyEvent.KEYCODE_SYM);
  }

  // Shift、Ctrl、Alt、Meta等修饰键在点击时是否触发锁定
  public boolean isShiftLock() {
    String s = getClick().getShiftLock();
    // shift_lock #ascii_long: 英文長按中文單按鎖定, long: 長按鎖定, click: 單按鎖定
    if ("long".equals(s)) return false;
    if ("click".equals(s)) return true;
    if ("ascii_long".equals(s)) return !Rime.isAsciiMode();
    return false;
  }

  /**
   * @param type 同文按键模式（点击/长按/滑动）
   * @return
   */
  public boolean sendBindings(int type) {
    Event e = null;
    if (type != KeyEventType.CLICK.ordinal() && type >= 0 && type <= EVENT_NUM) e = events[type];
    if (e != null) return true;
    if (events[KeyEventType.ASCII.ordinal()] != null && Rime.isAsciiMode()) return false;
    if (send_bindings) {
      if (events[KeyEventType.PAGING.ordinal()] != null && Rime.isPaging()) return true;
      if (events[KeyEventType.HAS_MENU.ordinal()] != null && Rime.hasMenu()) return true;
      if (events[KeyEventType.COMPOSING.ordinal()] != null && Rime.isComposing()) return true;
    }
    return false;
  }

  private Event getEvent() {
    if (events[KeyEventType.ASCII.ordinal()] != null && Rime.isAsciiMode())
      return events[KeyEventType.ASCII.ordinal()];
    if (events[KeyEventType.PAGING.ordinal()] != null && Rime.isPaging())
      return events[KeyEventType.PAGING.ordinal()];
    if (events[KeyEventType.HAS_MENU.ordinal()] != null && Rime.hasMenu())
      return events[KeyEventType.HAS_MENU.ordinal()];
    if (events[KeyEventType.COMPOSING.ordinal()] != null && Rime.isComposing())
      return events[KeyEventType.COMPOSING.ordinal()];
    return getClick();
  }

  public Event getClick() {
    return events[KeyEventType.CLICK.ordinal()];
  }

  public KeyEventType getLongClickType() {
    final int code = getCode();
    if (code >= KeyEvent.KEYCODE_A && code <= KeyEvent.KEYCODE_Z
        && !AppPrefs.defaultInstance().getTypeDuck().getSymbolsOnQwerty()) return null;
    if (events[KeyEventType.ASCII_LONG_CLICK.ordinal()] != null && Rime.isAsciiMode())
      return KeyEventType.ASCII_LONG_CLICK;
    if (getEventType() != KeyEventType.CLICK)
      return KeyEventType.CLICK;
    if (events[KeyEventType.LONG_CLICK.ordinal()] != null)
      return KeyEventType.LONG_CLICK;
    if (events[KeyEventType.ASCII.ordinal()] != null && !mKeyboard.getName().equals("default"))
      return Rime.isAsciiMode() ? KeyEventType.CLICK : KeyEventType.ASCII;
    return null;
  }

  public boolean hasEvent(int i) {
    return events[i] != null;
  }

  public Event getEvent(int i) {
    Event e = null;
    if (i != KeyEventType.CLICK.ordinal() && i >= 0 && i <= EVENT_NUM) e = events[i];
    if (e != null) return e;
    if (events[KeyEventType.ASCII.ordinal()] != null && Rime.isAsciiMode())
      return events[KeyEventType.ASCII.ordinal()];
    if (send_bindings) {
      if (events[KeyEventType.PAGING.ordinal()] != null && Rime.isPaging())
        return events[KeyEventType.PAGING.ordinal()];
      if (events[KeyEventType.HAS_MENU.ordinal()] != null && Rime.hasMenu())
        return events[KeyEventType.HAS_MENU.ordinal()];
      if (events[KeyEventType.COMPOSING.ordinal()] != null && Rime.isComposing())
        return events[KeyEventType.COMPOSING.ordinal()];
    }
    return getClick();
  }

  public KeyEventType getEventType() {
    if (events[KeyEventType.PAGING.ordinal()] != null && Rime.isPaging())
      return KeyEventType.PAGING;
    if (events[KeyEventType.HAS_MENU.ordinal()] != null && Rime.hasMenu())
      return KeyEventType.HAS_MENU;
    return KeyEventType.CLICK;
  }

  public int getCode() {
    return getClick().getCode();
  }

  public int getCode(int type) {
    return getEvent(type).getCode();
  }

  public String getLabel() {
    Event event = getEvent();
    if (event == getClick()
        && (events[KeyEventType.ASCII.ordinal()] == null && !Rime.showAsciiPunch())) {
      if (!TextUtils.isEmpty(cangjieLabel) && Rime.schemaIsCangjie())
        return cangjieLabel;
      if (!TextUtils.isEmpty(label))
        return label; // 中文狀態顯示標籤
    }
    return event.getLabel();
  }

  public String getPreviewText(int type) {
    if (type == KeyEventType.CLICK.ordinal()) return getEvent().getPreviewText();
    return getEvent(type).getPreviewText();
  }

  public String getSymbolLabel() {
    if (!mKeyboard.getName().equals("default")
        || !AppPrefs.defaultInstance().getTypeDuck().getSymbolsOnQwerty()) return "";
    if (labelSymbol.isEmpty()) {
      KeyEventType longClickType = getLongClickType();
      if (longClickType != null) return events[longClickType.ordinal()].getLabel();
    }
    return labelSymbol;
  }
}
