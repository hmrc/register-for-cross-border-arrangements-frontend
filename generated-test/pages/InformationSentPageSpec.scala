package pages

import pages.behaviours.PageBehaviours


class InformationSentPageSpec extends PageBehaviours {

  "InformationSentPage" - {

    beRetrievable[String](InformationSentPage)

    beSettable[String](InformationSentPage)

    beRemovable[String](InformationSentPage)
  }
}
