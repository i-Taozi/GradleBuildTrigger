package crazypants.enderio.invpanel.client;

import java.text.Collator;

class CountComparator extends NameComparator {

  public CountComparator(Collator collator) {
    super(collator);
  }

  @Override
  public int compare(ItemEntry a, ItemEntry b) {
    int res = b.getCount() - a.getCount();
    if(res == 0) {
      res = super.compare(a, b);
    }
    return res;
  }

}
