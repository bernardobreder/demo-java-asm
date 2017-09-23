package emuasm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringMaker {

  protected char[] value;

  protected int offset;

  protected int count;

  public StringMaker() {
    this(16);
  }

  public StringMaker(int capacity) {
    value = new char[capacity];
  }

  public StringMaker append(String str) {
    if (str == null) {
      str = "null";
    }
    int len = str.length();
    ensureCapacityInternal(count + len);
    str.getChars(0, len, value, count + offset);
    count += len;
    return this;
  }

  public StringMaker append(char c) {
    ensureCapacityInternal(count + 1);
    value[count++] = c;
    return this;
  }

  public StringMaker deleteFromEnd(int length) {
    count -= length;
    return this;
  }

  public StringMaker trim() {
    while (value[offset] <= ' ') {
      offset++;
      count--;
    }
    while (value[offset + count - 1] <= ' ') {
      count--;
    }
    return this;
  }

  public String[] split(char c, boolean hasEmptyItem) {
    List<String> list = new ArrayList<String>();
    StringMaker sb = new StringMaker();
    for (int n = offset; n < count; n++) {
    }
    return list.toArray(new String[list.size()]);
  }

  public StringMaker clear() {
    offset = 0;
    count = 0;
    return this;
  }

  public int length() {
    return count;
  }

  public boolean endsWith(String suffix) {
    int length = suffix.length();
    if (count < length) {
      return false;
    }
    for (int n = 0; n < length; n++) {
      int index = count - length + offset + n;
      if (value[index] != suffix.charAt(n)) {
        return false;
      }
    }
    return true;
  }

  public void trimToSize() {
    if (count < value.length) {
      value = Arrays.copyOf(value, count);
    }
  }

  protected void ensureCapacityInternal(int minimumCapacity) {
    if (minimumCapacity - value.length > 0) {
      expandCapacity(minimumCapacity);
    }
  }

  void expandCapacity(int minimumCapacity) {
    int newCapacity = value.length * 2 + 2;
    if (newCapacity - minimumCapacity < 0) {
      newCapacity = minimumCapacity;
    }
    if (newCapacity < 0) {
      if (minimumCapacity < 0) {
        throw new OutOfMemoryError();
      }
      newCapacity = Integer.MAX_VALUE;
    }
    value = Arrays.copyOf(value, newCapacity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    StringMaker other = (StringMaker) obj;
    if (count != other.count) {
      return false;
    }
    for (int n = offset; n < count; n++) {
      if (value[n] != other.value[other.offset + n]) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int h = count;
    for (int n = 0; n < count; n++) {
      h = 31 * h + value[offset + n];
    }
    return h;
  };

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new String(value, offset, count);
  }

  public static void main(String[] args) {
    System.out.println(new StringMaker(2).append("  Bernardo ").append(
      "Breder ").trim());
  }

}
