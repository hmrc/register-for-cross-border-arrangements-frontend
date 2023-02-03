package pages

import pages.behaviours.PageBehaviours


class IInformation==IinformationsetnPageSpec extends PageBehaviours {

  "IInformation==IinformationsetnPage" - {

    beRetrievable[String](IInformation==IinformationsetnPage)

    beSettable[String](IInformation==IinformationsetnPage)

    beRemovable[String](IInformation==IinformationsetnPage)
  }
}
