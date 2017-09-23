package emuasm.gui;

import java.util.Comparator;

public class RegisterComparator implements Comparator<String> {

  /**
   * {@inheritDoc}
   */
  @Override
  public int compare(String o1, String o2) {
    boolean eax1 = o1.length() == 3 && o1.startsWith("e") && o1.endsWith("x");
    boolean eax2 = o2.length() == 3 && o2.startsWith("e") && o2.endsWith("x");
    if (eax1 && eax2) {
      return o1.compareTo(o2);
    }
    else if (eax1) {
      return -1;
    }
    else if (eax2) {
      return 1;
    }
    boolean ep1 = o1.length() == 3 && o1.startsWith("e") && o1.endsWith("p");
    boolean ep2 = o2.length() == 3 && o2.startsWith("e") && o2.endsWith("p");
    if (ep1 && ep2) {
      return o1.compareTo(o2);
    }
    else if (ep1) {
      return -1;
    }
    else if (ep2) {
      return 1;
    }
    boolean s1 = o1.length() == 2 && o1.endsWith("s");
    boolean s2 = o2.length() == 2 && o2.endsWith("s");
    if (s1 && s2) {
      return o1.compareTo(o2);
    }
    else if (s1) {
      return -1;
    }
    else if (s2) {
      return 1;
    }
    boolean i1 = o1.length() == 3 && o1.endsWith("i");
    boolean i2 = o2.length() == 3 && o2.endsWith("i");
    if (i1 && i2) {
      return o1.compareTo(o2);
    }
    else if (i1) {
      return -1;
    }
    else if (i2) {
      return 1;
    }
    boolean eflags1 = o1.equals("eflags");
    boolean eflags2 = o2.equals("eflags");
    if (eflags1 || eflags2) {
      if (eflags1) {
        return o1.compareTo(o2);
      }
      else {
        return -1;
      }
    }
    return o1.compareTo(o2);
  }

}