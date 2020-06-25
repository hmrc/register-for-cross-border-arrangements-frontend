package pages

import models.SelectAddress
import pages.behaviours.PageBehaviours

class SelectAddressSpec extends PageBehaviours {

  "SelectAddressPage" - {

    beRetrievable[SelectAddress](SelectAddressPage)

    beSettable[SelectAddress](SelectAddressPage)

    beRemovable[SelectAddress](SelectAddressPage)
  }
}
