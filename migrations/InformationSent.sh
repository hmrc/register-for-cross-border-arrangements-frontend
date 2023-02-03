#!/bin/bash

echo ""
echo "Applying migration InformationSent"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /informationSent                        controllers.InformationSentController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /informationSent                        controllers.InformationSentController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeInformationSent                  controllers.InformationSentController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeInformationSent                  controllers.InformationSentController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "informationSent.title = informationSent" >> ../conf/messages.en
echo "informationSent.heading = informationSent" >> ../conf/messages.en
echo "informationSent.checkYourAnswersLabel = informationSent" >> ../conf/messages.en
echo "informationSent.error.required = Enter informationSent" >> ../conf/messages.en
echo "informationSent.error.length = InformationSent must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInformationSentUserAnswersEntry: Arbitrary[(InformationSentPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[InformationSentPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInformationSentPage: Arbitrary[InformationSentPage.type] =";\
    print "    Arbitrary(InformationSentPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(InformationSentPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def informationSent: Option[Row] = userAnswers.get(InformationSentPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"informationSent.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.InformationSentController.onPageLoad(CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"informationSent.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration InformationSent completed"
