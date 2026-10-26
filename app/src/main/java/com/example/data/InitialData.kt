package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object InitialData {

    private fun forms(vararg items: WordSubForm): String {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("georgian", item.georgian)
                put("transcription", item.transcription)
                put("russian", item.russian)
                put("grammaticalNote", item.grammaticalNote)
                put("isEncountered", item.isEncountered)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    val CATEGORIES = listOf(
        CategoryInfo("all", "Все слова", "📚", "Полный словарь всех частей речи"),
        CategoryInfo("food", "Кафе и кухня", "🥟", "Молоко, вода, хлеб, хачапури, заказ"),
        CategoryInfo("greetings", "Приветствия и этикет", "👋", "Здравствуйте, спасибо, пожалуйста"),
        CategoryInfo("city", "Город и транспорт", "🚕", "Дом, улица, Тбилиси, такси"),
        CategoryInfo("grammar", "Грамматика и служебные", "🧩", "Глаголы, союзы, местоимения, частицы")
    )

    data class CategoryInfo(
        val id: String,
        val title: String,
        val emoji: String,
        val description: String
    )

    val DEFAULT_UNITS = listOf(
        UnitEntity("greetings", "Приветствия и этикет", "👋", "Базовые фразы для старта любого диалога", 0),
        UnitEntity("food", "Кафе и кухня", "🥟", "Еда, напитки, формы склонения и заказ", 1),
        UnitEntity("city", "Город и транспорт", "🚕", "Места, направления и городские слова", 2),
        UnitEntity("grammar", "Грамматика и служебные слова", "🧩", "Глаголы связки, союзы и местоимения", 3)
    )

    val DEFAULT_WORDS = listOf(
        // =========================================================================
        // 1. СУЩЕСТВИТЕЛЬНЫЕ (NOUNS) со всеми склонениями и формами
        // =========================================================================
        WordEntity(
            georgian = "რძე",
            transcription = "rdze / рдзе",
            russian = "молоко",
            synonyms = "молоко, молочко",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🥛",
            exampleGeorgian = "მე მინდა ცხელი ჩაი რძით.",
            exampleRussian = "Я хочу горячий чай с молоком.",
            subFormsJson = forms(
                WordSubForm("რძე", "rdze / рдзе", "молоко", "именительный падеж (кто? что?)", true),
                WordSubForm("რძით", "rdzit / рдзит", "с молоком / молоком", "творительный падеж (-ით, чем? с чем?)", true),
                WordSubForm("რძეში", "rdzeshi / рдзеши", "в молоке", "местный падеж (-ში, в чём?)", true),
                WordSubForm("რძის", "rdzis / рдзис", "молока", "родительный падеж (-ის, кого? чего?)", true),
                WordSubForm("რძეს", "rdzes / рдзес", "молоку / молоко", "дательный падеж (-ს, кому? чему?)", false)
            )
        ),
        WordEntity(
            georgian = "წყალი",
            transcription = "tsqali / цкали",
            russian = "вода",
            synonyms = "вода, водичка, водица",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "💧",
            exampleGeorgian = "ერთი ბოთლი ცივი წყალი მომიტანეთ.",
            exampleRussian = "Принесите одну бутылку холодной воды.",
            subFormsJson = forms(
                WordSubForm("წყალი", "tsqali / цкали", "вода", "именительный падеж", true),
                WordSubForm("წყალს", "tsqals / цкалс", "воду", "дательный падеж (прямое дополнение)", true),
                WordSubForm("წყლის", "tsqlis / цклис", "воды", "родительный падеж (-ის)", true),
                WordSubForm("წყლით", "tsqlit / цклит", "водой / с водой", "творительный падеж (-ით)", true),
                WordSubForm("წყალში", "tsqalshi / цкалши", "в воде", "местный падеж (-ში)", false)
            )
        ),
        WordEntity(
            georgian = "ყავა",
            transcription = "qava / кава",
            russian = "кофе",
            synonyms = "кофе, кофей",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "☕",
            exampleGeorgian = "ერთი ცხელი ყავა რძით, გთხოვთ.",
            exampleRussian = "Один горячий кофе с молоком, пожалуйста.",
            subFormsJson = forms(
                WordSubForm("ყავა", "qava / кава", "кофе", "именительный падеж", true),
                WordSubForm("ყავას", "qavas / кавас", "кофе", "дательный падеж (пью кофе)", true),
                WordSubForm("ყავით", "qavit / кавит", "с кофе", "творительный падеж", true),
                WordSubForm("ყავის", "qavis / кавис", "кофе (зерна кофе)", "родительный падеж", false),
                WordSubForm("ყავაში", "qavashi / каваши", "в кофе", "местный падеж", false)
            )
        ),
        WordEntity(
            georgian = "ჩაი",
            transcription = "chai / чаи",
            russian = "чай",
            synonyms = "чай, чаёк",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🍵",
            exampleGeorgian = "შავი ჩაი ლიმონით, გთხოვთ.",
            exampleRussian = "Чёрный чай с лимоном, пожалуйста.",
            subFormsJson = forms(
                WordSubForm("ჩაი", "chai / чаи", "чай", "именительный падеж", true),
                WordSubForm("ჩაის", "chais / чаис", "чай (пью чай)", "дательный падеж", true),
                WordSubForm("ჩაით", "chait / чаит", "с чаем", "творительный падеж", true),
                WordSubForm("ჩაიში", "chaishi / чаиши", "в чае", "местный падеж", false)
            )
        ),
        WordEntity(
            georgian = "ღვინო",
            transcription = "ghvino / гхвино",
            russian = "вино",
            synonyms = "вино, винцо",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🍷",
            exampleGeorgian = "ეს წითელი ღვინო ძალიან კარგია.",
            exampleRussian = "Это красное вино очень хорошее.",
            subFormsJson = forms(
                WordSubForm("ღვინო", "ghvino / гхвино", "вино", "именительный падеж", true),
                WordSubForm("ღვინოს", "ghvinos / гхвинос", "вино (пьем вино)", "дательный падеж", true),
                WordSubForm("ღვინის", "ghvinis / гхвинис", "вина (бокал вина)", "родительный падеж", true),
                WordSubForm("ღვინით", "ghvinit / гхвинит", "с вином", "творительный падеж", false),
                WordSubForm("ღვინოში", "ghvinoshi / гхвиноши", "в вине", "местный падеж", false)
            )
        ),
        WordEntity(
            georgian = "პური",
            transcription = "puri / пури",
            russian = "хлеб",
            synonyms = "хлеб, хлебушек",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🍞",
            exampleGeorgian = "ახალი თბილი პური მინდა.",
            exampleRussian = "Я хочу свежий тёплый хлеб.",
            subFormsJson = forms(
                WordSubForm("პური", "puri / пури", "хлеб", "именительный падеж", true),
                WordSubForm("პურს", "purs / пурс", "хлеб (ем хлеб)", "дательный падеж", true),
                WordSubForm("პურით", "purit / пурит", "с хлебом", "творительный падеж", true),
                WordSubForm("პურის", "puris / пурис", "хлеба", "родительный падеж", false)
            )
        ),
        WordEntity(
            georgian = "ხაჭაპური",
            transcription = "khachapuri / хачапури",
            russian = "хачапури",
            synonyms = "хачапури",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🥧",
            exampleGeorgian = "ერთი აჭარული ხაჭაპური, გთხოვთ.",
            exampleRussian = "Один аджарский хачапури, пожалуйста.",
            subFormsJson = forms(
                WordSubForm("ხაჭაპური", "khachapuri / хачапури", "хачапури", "именительный падеж", true),
                WordSubForm("ხაჭაპურს", "khachapurs / хачапурс", "хачапури", "дательный падеж", true),
                WordSubForm("ხაჭაპურით", "khachapurit / хачапурит", "с хачапури", "творительный падеж", false)
            )
        ),
        WordEntity(
            georgian = "ხინკალი",
            transcription = "khinkali / хинкали",
            russian = "хинкали",
            synonyms = "хинкали",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🥟",
            exampleGeorgian = "ათი ხინკალი გვინდა.",
            exampleRussian = "Нам нужно десять хинкали.",
            subFormsJson = forms(
                WordSubForm("ხინკალი", "khinkali / хинкали", "хинкали", "именительный падеж", true),
                WordSubForm("ხინკალს", "khinkals / хинкалс", "хинкали", "дательный падеж", true),
                WordSubForm("ხინკლით", "khinklit / хинклит", "с хинкали", "творительный падеж", false)
            )
        ),
        WordEntity(
            georgian = "მეგობარი",
            transcription = "megobari / мегобари",
            russian = "друг",
            synonyms = "друг, приятель, товарищ",
            partOfSpeech = "NOUN",
            category = "city",
            emoji = "🤝",
            exampleGeorgian = "აქ არის ჩემი მეგობარი.",
            exampleRussian = "Здесь находится мой друг.",
            subFormsJson = forms(
                WordSubForm("მეგობარი", "megobari / мегобари", "друг", "именительный падеж", true),
                WordSubForm("მეგობარს", "megobars / мегобарс", "другу / друга", "дательный падеж", true),
                WordSubForm("მეგობართან", "megobartan / мегобартан", "с другом / у друга", "совместный падеж (-თან)", true),
                WordSubForm("მეგობრის", "megobris / мегобрис", "друга", "родительный падеж", false),
                WordSubForm("მეგობრები", "megobrebi / мегобреби", "друзья", "множественное число (-ები)", true)
            )
        ),
        WordEntity(
            georgian = "სახლი",
            transcription = "sakhli / сахли",
            russian = "дом",
            synonyms = "дом, домик, жилище",
            partOfSpeech = "NOUN",
            category = "city",
            emoji = "🏡",
            exampleGeorgian = "ჩვენ მივდივართ სახლში.",
            exampleRussian = "Мы идём домой.",
            subFormsJson = forms(
                WordSubForm("სახლი", "sakhli / сахли", "дом", "именительный падеж", true),
                WordSubForm("სახლში", "sakhlshi / сахлши", "дома / в доме / домой", "местный падеж (-ში)", true),
                WordSubForm("სახლიდან", "sakhlidan / сахлидан", "из дома", "исходный падеж (-იდან)", true),
                WordSubForm("სახლს", "sakhls / сахлс", "дому / дом", "дательный падеж", false)
            )
        ),
        WordEntity(
            georgian = "კაფე",
            transcription = "kafe / кафе",
            russian = "кафе",
            synonyms = "кафе, кофейня, ресторанчик",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "☕",
            exampleGeorgian = "დღეს მე მივდივარ კაფეში.",
            exampleRussian = "Сегодня я иду в кафе.",
            subFormsJson = forms(
                WordSubForm("კაფე", "kafe / кафе", "кафе", "именительный падеж", true),
                WordSubForm("კაფეში", "kafeshi / кафеши", "в кафе", "местный падеж (-ში)", true),
                WordSubForm("კაფედან", "kafedan / кафедан", "из кафе", "исходный падеж", false)
            )
        ),
        WordEntity(
            georgian = "თბილისი",
            transcription = "tbilisi / тбилиси",
            russian = "Тбилиси",
            synonyms = "Тбилиси, город Тбилиси",
            partOfSpeech = "NOUN",
            category = "city",
            emoji = "🇬🇪",
            exampleGeorgian = "გამარჯობა! მე ვარ თბილისში.",
            exampleRussian = "Здравствуйте! Я в Тбилиси.",
            subFormsJson = forms(
                WordSubForm("თბილისი", "tbilisi / тбилиси", "Тбилиси", "именительный падеж", true),
                WordSubForm("თბილისში", "tbilisshi / тбилисши", "в Тбилиси", "местный падеж (-ში)", true),
                WordSubForm("თბილისიდან", "tbilisidan / тбилисидан", "из Тбилиси", "исходный падеж (-იდან)", true)
            )
        ),
        WordEntity(
            georgian = "საქართველო",
            transcription = "saqartvelo / сакартвело",
            russian = "Грузия",
            synonyms = "Грузия, Сакартвело",
            partOfSpeech = "NOUN",
            category = "city",
            emoji = "🇬🇪",
            exampleGeorgian = "მე მიყვარს საქართველო.",
            exampleRussian = "Я люблю Грузию.",
            subFormsJson = forms(
                WordSubForm("საქართველო", "saqartvelo / сакартвело", "Грузия", "именительный падеж", true),
                WordSubForm("საქართველოში", "saqartveloshi / сакартвелоши", "в Грузии", "местный падеж (-ში)", true)
            )
        ),
        WordEntity(
            georgian = "ანგარიში",
            transcription = "angarishi / ангариши",
            russian = "счёт",
            synonyms = "счёт, чек, расчет",
            partOfSpeech = "NOUN",
            category = "food",
            emoji = "🧾",
            exampleGeorgian = "ანგარიში მომიტანეთ, გთხოვთ.",
            exampleRussian = "Принесите счёт, пожалуйста.",
            subFormsJson = forms(
                WordSubForm("ანგარიში", "angarishi / ангариши", "счёт", "именительный падеж", true),
                WordSubForm("ანგარიშს", "angarishs / ангаришс", "счёту", "дательный падеж", false)
            )
        ),

        // =========================================================================
        // 2. ГЛАГОЛЫ (VERBS) со всеми спряжениями и временами
        // =========================================================================
        WordEntity(
            georgian = "ყოფნა",
            transcription = "qopna / копна",
            russian = "быть",
            synonyms = "быть, являться, находиться",
            partOfSpeech = "VERB",
            category = "grammar",
            emoji = "👤",
            exampleGeorgian = "მე ვარ თბილისში, ის არის სახლში.",
            exampleRussian = "Я в Тбилиси, он дома.",
            subFormsJson = forms(
                WordSubForm("ვარ", "var / вар", "я есть / нахожусь", "1 л. ед.ч., наст. вр.", true),
                WordSubForm("ხარ", "khar / хар", "ты есть / находишься", "2 л. ед.ч., наст. вр.", true),
                WordSubForm("არის", "aris / арис", "он/она/оно есть", "3 л. ед.ч., наст. вр.", true),
                WordSubForm("ვართ", "vart / варт", "мы есть / находимся", "1 л. мн.ч., наст. вр.", true),
                WordSubForm("ხართ", "khart / харт", "вы есть", "2 л. мн.ч., наст. вр.", false),
                WordSubForm("არიან", "arian / ариан", "они есть", "3 л. мн.ч., наст. вр.", false),
                WordSubForm("იყო", "iqo / ико", "был / была / было", "прошедшее время", true),
                WordSubForm("იქნება", "iqneba / икнеба", "будет", "будущее время", false)
            )
        ),
        WordEntity(
            georgian = "ნდომა",
            transcription = "ndoma / ндома",
            russian = "хотеть",
            synonyms = "хотеть, желать",
            partOfSpeech = "VERB",
            category = "grammar",
            emoji = "💭",
            exampleGeorgian = "მე მინდა წყალი, შენ რა გინდა?",
            exampleRussian = "Я хочу воды, а ты чего хочешь?",
            subFormsJson = forms(
                WordSubForm("მინდა", "minda / минда", "я хочу", "1 л. ед.ч.", true),
                WordSubForm("გინდა", "ginda / гинда", "ты хочешь", "2 л. ед.ч.", true),
                WordSubForm("უნდა", "unda / унда", "он/она хочет (или нужно)", "3 л. ед.ч.", true),
                WordSubForm("გვინდა", "gvinda / гвинда", "мы хотим", "1 л. мн.ч.", true),
                WordSubForm("გინდათ", "gindat / гиндат", "вы хотите", "2 л. мн.ч.", false),
                WordSubForm("უნდათ", "undat / ундат", "они хотят", "3 л. мн.ч.", false)
            )
        ),
        WordEntity(
            georgian = "სიყვარული",
            transcription = "siqvaruli / сикварули",
            russian = "любить",
            synonyms = "любить, нравиться",
            partOfSpeech = "VERB",
            category = "grammar",
            emoji = "❤️",
            exampleGeorgian = "მე ძალიან მიყვარს საქართველო.",
            exampleRussian = "Я очень люблю Грузию.",
            subFormsJson = forms(
                WordSubForm("მიყვარს", "miqvars / микварс", "я люблю", "1 л. ед.ч.", true),
                WordSubForm("გიყვარს", "giqvars / гикварс", "ты любишь", "2 л. ед.ч.", true),
                WordSubForm("უყვარს", "uqvars / укварс", "он/она любит", "3 л. ед.ч.", true),
                WordSubForm("გვიყვარს", "gviqvars / гвикварс", "мы любим", "1 л. мн.ч.", false)
            )
        ),
        WordEntity(
            georgian = "წასვლა",
            transcription = "tsasvla / цасвла",
            russian = "идти",
            synonyms = "идти, ехать, направляться",
            partOfSpeech = "VERB",
            category = "grammar",
            emoji = "🚶",
            exampleGeorgian = "დღეს მე მივდივარ კაფეში.",
            exampleRussian = "Сегодня я иду в кафе.",
            subFormsJson = forms(
                WordSubForm("მივდივარ", "mivdivar / мивдивар", "я иду / еду", "1 л. ед.ч.", true),
                WordSubForm("მიდიხარ", "midikhar / мидихар", "ты идёшь / едешь", "2 л. ед.ч.", true),
                WordSubForm("მიდის", "midis / мидис", "он/она идёт", "3 л. ед.ч.", true),
                WordSubForm("მივდივართ", "mivdivart / мивдиварт", "мы идём / едем", "1 л. мн.ч.", true),
                WordSubForm("წავიდეთ", "tsavidet / цавидет", "пойдёмте / давай пойдём", "побудительное наклонение", true)
            )
        ),
        WordEntity(
            georgian = "დალევა",
            transcription = "daleva / далева",
            russian = "пить",
            synonyms = "пить, выпить",
            partOfSpeech = "VERB",
            category = "food",
            emoji = "🥤",
            exampleGeorgian = "ჩემი მეგობარი სვამს ყავას.",
            exampleRussian = "Мой друг пьёт кофе.",
            subFormsJson = forms(
                WordSubForm("ვსვამ", "vsvam / всвам", "я пью", "1 л. ед.ч.", true),
                WordSubForm("სვამ", "svam / свам", "ты пьёшь", "2 л. ед.ч.", true),
                WordSubForm("სვამს", "svams / свамс", "он/она пьёт", "3 л. ед.ч.", true),
                WordSubForm("დავლიე", "davlie / давлиэ", "я выпил", "прошедшее время", true)
            )
        ),
        WordEntity(
            georgian = "ჭამა",
            transcription = "chama / чама",
            russian = "кушать",
            synonyms = "кушать, есть, отведать",
            partOfSpeech = "VERB",
            category = "food",
            emoji = "🍽️",
            exampleGeorgian = "ჩვენ ვჭამთ გემრიელ ხაჭაპურს.",
            exampleRussian = "Мы едим вкусный хачапури.",
            subFormsJson = forms(
                WordSubForm("ვჭამ", "vcham / вчам", "я ем", "1 л. ед.ч.", true),
                WordSubForm("ჭამ", "cham / чам", "ты ешь", "2 л. ед.ч.", true),
                WordSubForm("ჭამს", "chams / чамс", "он/она ест", "3 л. ед.ч.", true),
                WordSubForm("ვჭამთ", "vchamt / вчамт", "мы едим", "1 л. мн.ч.", true)
            )
        ),
        WordEntity(
            georgian = "ლაპარაკი",
            transcription = "laparaki / лапараки",
            russian = "говорить",
            synonyms = "говорить, разговаривать, беседовать",
            partOfSpeech = "VERB",
            category = "grammar",
            emoji = "🗣️",
            exampleGeorgian = "მე ვლაპარაკობ ქართულად.",
            exampleRussian = "Я говорю по-грузински.",
            subFormsJson = forms(
                WordSubForm("ვლაპარაკობ", "vlaparakob / влапаракоб", "я говорю", "1 л. ед.ч.", true),
                WordSubForm("ლაპარაკობ", "laparakob / лапаракоб", "ты говоришь", "2 л. ед.ч.", true),
                WordSubForm("ლაპარაკობს", "laparakobs / лапаракобс", "он/она говорит", "3 л. ед.ч.", false)
            )
        ),

        // =========================================================================
        // 3. ПРИЛАГАТЕЛЬНЫЕ (ADJECTIVES)
        // =========================================================================
        WordEntity(
            georgian = "ლამაზი",
            transcription = "lamazi / ламази",
            russian = "красивый",
            synonyms = "красивый, красивая, красивое, прекрасный, прекрасная",
            partOfSpeech = "ADJECTIVE",
            category = "grammar",
            emoji = "🌸",
            exampleGeorgian = "თბილისი ძალიან ლამაზი ქალაქია.",
            exampleRussian = "Тбилиси — очень красивый город.",
            subFormsJson = forms(
                WordSubForm("ლამაზი", "lamazi / ламази", "красивый / красивая", "основная форма", true),
                WordSubForm("ლამაზად", "lamazad / ламазад", "красиво", "наречная форма (-ად)", true),
                WordSubForm("ლამაზ", "lamaz / ламаз", "красив- (перед сущ.)", "усечённая форма", false)
            )
        ),
        WordEntity(
            georgian = "დიდი",
            transcription = "didi / диди",
            russian = "большой",
            synonyms = "большой, большая, большое, крупный",
            partOfSpeech = "ADJECTIVE",
            category = "grammar",
            emoji = "🐘",
            exampleGeorgian = "დიდი მადლობა დახმარებისთვის.",
            exampleRussian = "Большое спасибо за помощь.",
            subFormsJson = forms(
                WordSubForm("დიდი", "didi / диди", "большой", "основная форма", true),
                WordSubForm("დიდ", "did / дид", "больш- (перед сущ.)", "усечённая форма", true)
            )
        ),
        WordEntity(
            georgian = "პატარა",
            transcription = "patara / патара",
            russian = "маленький",
            synonyms = "маленький, маленькая, небольшая, небольшой",
            partOfSpeech = "ADJECTIVE",
            category = "grammar",
            emoji = "🐣",
            exampleGeorgian = "ეს არის პატარა მყუდრო კაფე.",
            exampleRussian = "Это маленькое уютное кафе.",
            subFormsJson = forms(
                WordSubForm("პატარა", "patara / патара", "маленький", "основная форма", true)
            )
        ),
        WordEntity(
            georgian = "კარგი",
            transcription = "kargi / карги",
            russian = "хороший",
            synonyms = "хороший, хорошая, хорошее, ладный",
            partOfSpeech = "ADJECTIVE",
            category = "grammar",
            emoji = "👍",
            exampleGeorgian = "ეს წითელი ღვინო ძალიან კარგია.",
            exampleRussian = "Это красное вино очень хорошее.",
            subFormsJson = forms(
                WordSubForm("კარგი", "kargi / карги", "хороший", "основная форма", true),
                WordSubForm("კარგად", "kargad / каргад", "хорошо", "наречная форма (-ად)", true),
                WordSubForm("კარგ", "karg / карг", "хорош- (перед сущ.)", "усечённая форма", false)
            )
        ),
        WordEntity(
            georgian = "ცუდი",
            transcription = "tsudi / цуди",
            russian = "плохой",
            synonyms = "плохой, плохая, плохое, дурной",
            partOfSpeech = "ADJECTIVE",
            category = "grammar",
            emoji = "👎",
            exampleGeorgian = "დღეს ცუდი ამინდია.",
            exampleRussian = "Сегодня плохая погода.",
            subFormsJson = forms(
                WordSubForm("ცუდი", "tsudi / цуди", "плохой", "основная форма", true),
                WordSubForm("ცუდად", "tsudad / цудад", "плохо", "наречная форма (-ად)", true)
            )
        ),
        WordEntity(
            georgian = "გემრიელი",
            transcription = "gemrieli / гемриели",
            russian = "вкусный",
            synonyms = "вкусный, вкусная, вкусное, аппетитный",
            partOfSpeech = "ADJECTIVE",
            category = "food",
            emoji = "😋",
            exampleGeorgian = "ეს კერძი ძალიან გემრიელია!",
            exampleRussian = "Это блюдо очень вкусное!",
            subFormsJson = forms(
                WordSubForm("გემრიელი", "gemrieli / гемриели", "вкусный", "основная форма", true),
                WordSubForm("გემრიელია", "gemrielia / гемриелиа", "вкусно (со связкой -а)", "предикативная форма", true)
            )
        ),
        WordEntity(
            georgian = "ცხელი",
            transcription = "tskheli / цхели",
            russian = "горячий",
            synonyms = "горячий, горячая, горячее, жаркий",
            partOfSpeech = "ADJECTIVE",
            category = "food",
            emoji = "🔥",
            exampleGeorgian = "ერთი ცხელი ჩაი რძით, გთხოვთ.",
            exampleRussian = "Один горячий чай с молоком, пожалуйста.",
            subFormsJson = forms(
                WordSubForm("ცხელი", "tskheli / цхели", "горячий", "основная форма", true),
                WordSubForm("ცხელია", "tskhelia / цхелиа", "горячо", "предикативная форма", true)
            )
        ),
        WordEntity(
            georgian = "ცივი",
            transcription = "tsivi / циви",
            russian = "холодный",
            synonyms = "холодный, холодная, холодное, студёный",
            partOfSpeech = "ADJECTIVE",
            category = "food",
            emoji = "❄️",
            exampleGeorgian = "ერთი ბოთლი ცივი წყალი მომიტანეთ.",
            exampleRussian = "Принесите одну бутылку холодной воды.",
            subFormsJson = forms(
                WordSubForm("ცივი", "tsivi / циви", "холодный", "основная форма", true),
                WordSubForm("ცივია", "tsivia / цивиа", "холодно", "предикативная форма", true)
            )
        ),

        // =========================================================================
        // 3.1. ЧИСЛИТЕЛЬНЫЕ (NUMERALS)
        // =========================================================================
        WordEntity(
            georgian = "ერთი",
            transcription = "erti / эрти",
            russian = "один",
            synonyms = "один, одна, одно, единичка",
            partOfSpeech = "NUMERAL",
            category = "general",
            emoji = "1️⃣",
            exampleGeorgian = "",
            exampleRussian = "",
            subFormsJson = forms(
                WordSubForm("ერთს", "erts / эртс", "одному / одну", "дательный падеж", false),
                WordSubForm("ერთად", "ertad / эртад", "вместе", "обстоятельственный падеж", false)
            )
        ),
        WordEntity(
            georgian = "ორი",
            transcription = "ori / ори",
            russian = "два",
            synonyms = "два, две, двое",
            partOfSpeech = "NUMERAL",
            category = "general",
            emoji = "2️⃣",
            exampleGeorgian = "",
            exampleRussian = "",
            subFormsJson = forms(
                WordSubForm("ორს", "ors / орс", "двум / двоих", "дательный падеж", false)
            )
        ),
        WordEntity(
            georgian = "სამი",
            transcription = "sami / сами",
            russian = "три",
            synonyms = "три, трое",
            partOfSpeech = "NUMERAL",
            category = "general",
            emoji = "3️⃣",
            exampleGeorgian = "",
            exampleRussian = "",
            subFormsJson = forms(
                WordSubForm("სამს", "sams / самс", "трём / троих", "дательный падеж", false)
            )
        ),

        // =========================================================================
        // 4. МЕСТОИМЕНИЯ (PRONOUNS)
        // =========================================================================
        WordEntity(
            georgian = "მე",
            transcription = "me / ме",
            russian = "я",
            synonyms = "я",
            partOfSpeech = "PRONOUN",
            category = "grammar",
            emoji = "🙋",
            exampleGeorgian = "მე ვარ თბილისში.",
            exampleRussian = "Я нахожусь в Тбилиси.",
            subFormsJson = forms(
                WordSubForm("მე", "me / ме", "я", "именительный падеж", true),
                WordSubForm("ჩემთვის", "chemtvis / чемтвис", "для меня", "послелог -თვის", true),
                WordSubForm("ჩემთან", "chemtan / чемтан", "со мной / у меня", "послелог -თან", false)
            )
        ),
        WordEntity(
            georgian = "შენ",
            transcription = "shen / шен",
            russian = "ты",
            synonyms = "ты",
            partOfSpeech = "PRONOUN",
            category = "grammar",
            emoji = "👉",
            exampleGeorgian = "შენ სად მიდიხარ?",
            exampleRussian = "Куда ты идёшь?",
            subFormsJson = forms(
                WordSubForm("შენ", "shen / шен", "ты", "именительный падеж", true),
                WordSubForm("შენთვის", "shentvis / шентвис", "для тебя", "послелог -თვის", true),
                WordSubForm("შენთან", "shentan / шентан", "с тобой / у тебя", "послелог -თან", false)
            )
        ),
        WordEntity(
            georgian = "ის",
            transcription = "is / ис",
            russian = "он / она / оно",
            synonyms = "он, она, оно, тот, та",
            partOfSpeech = "PRONOUN",
            category = "grammar",
            emoji = "👤",
            exampleGeorgian = "ის არის ჩემი მეგობარი.",
            exampleRussian = "Он (или она) — мой друг.",
            subFormsJson = forms(
                WordSubForm("ის", "is / ис", "он / она / оно", "именительный падеж", true),
                WordSubForm("მას", "mas / мас", "ему / ей / его", "дательный падеж", true),
                WordSubForm("მისთვის", "mistvis / миствис", "для него / неё", "послелог -თვის", false)
            )
        ),
        WordEntity(
            georgian = "ჩვენ",
            transcription = "chven / чвен",
            russian = "мы",
            synonyms = "мы",
            partOfSpeech = "PRONOUN",
            category = "grammar",
            emoji = "👥",
            exampleGeorgian = "ჩვენ მივდივართ კაფეში.",
            exampleRussian = "Мы идём в кафе.",
            subFormsJson = forms(
                WordSubForm("ჩვენ", "chven / чвен", "мы", "именительный падеж", true),
                WordSubForm("ჩვენთვის", "chventvis / чвентвис", "для нас", "послелог", true)
            )
        ),
        WordEntity(
            georgian = "ეს",
            transcription = "es / эс",
            russian = "это / этот",
            synonyms = "это, этот, эта, се",
            partOfSpeech = "PRONOUN",
            category = "grammar",
            emoji = "👇",
            exampleGeorgian = "ეს კერძი ძალიან გემრიელია.",
            exampleRussian = "Это блюдо очень вкусное.",
            subFormsJson = forms(
                WordSubForm("ეს", "es / эс", "это / этот / эта", "именительный падеж", true),
                WordSubForm("ამას", "amas / амас", "этому / это", "дательный падеж", false)
            )
        ),
        WordEntity(
            georgian = "ჩემი",
            transcription = "chemi / чеми",
            russian = "мой",
            synonyms = "мой, моя, моё, мои",
            partOfSpeech = "PRONOUN",
            category = "grammar",
            emoji = "🙋",
            exampleGeorgian = "ეს არის ჩემი მეგობარი.",
            exampleRussian = "Это мой друг.",
            subFormsJson = forms(
                WordSubForm("ჩემი", "chemi / чеми", "мой / моя / моё", "основная форма", true),
                WordSubForm("ჩემს", "chems / чемс", "моему / мою", "дательный падеж", false)
            )
        ),

        // =========================================================================
        // 5. СОЮЗЫ (CONJUNCTIONS)
        // =========================================================================
        WordEntity(
            georgian = "და",
            transcription = "da / да",
            russian = "и",
            synonyms = "и, а, да",
            partOfSpeech = "CONJUNCTION",
            category = "grammar",
            emoji = "➕",
            exampleGeorgian = "ჩაი რძით და ხაჭაპური.",
            exampleRussian = "Чай с молоком и хачапури.",
            subFormsJson = forms(
                WordSubForm("და", "da / да", "и / а", "соединительный союз", true)
            )
        ),
        WordEntity(
            georgian = "მაგრამ",
            transcription = "magram / маграм",
            russian = "но",
            synonyms = "но, однако, зато",
            partOfSpeech = "CONJUNCTION",
            category = "grammar",
            emoji = "🔄",
            exampleGeorgian = "ცხელია, მაგრამ ძალიან გემრიელია.",
            exampleRussian = "Горячо, но очень вкусно.",
            subFormsJson = forms(
                WordSubForm("მაგრამ", "magram / маграм", "но / однако", "противительный союз", true)
            )
        ),
        WordEntity(
            georgian = "თუ",
            transcription = "tu / ту",
            russian = "если / или",
            synonyms = "если, или, ежели, коль",
            partOfSpeech = "CONJUNCTION",
            category = "grammar",
            emoji = "❓",
            exampleGeorgian = "ჩაი გინდა თუ ყავა?",
            exampleRussian = "Ты хочешь чай или кофе?",
            subFormsJson = forms(
                WordSubForm("თუ", "tu / ту", "если / или", "разделительный / условный союз", true)
            )
        ),
        WordEntity(
            georgian = "რომ",
            transcription = "rom / ром",
            russian = "что / чтобы",
            synonyms = "что, чтобы, дабы",
            partOfSpeech = "CONJUNCTION",
            category = "grammar",
            emoji = "🔗",
            exampleGeorgian = "მე ვიცი, რომ თბილისი ლამაზია.",
            exampleRussian = "Я знаю, что Тбилиси красивый.",
            subFormsJson = forms(
                WordSubForm("რომ", "rom / ром", "что / чтобы", "подчинительный союз", true)
            )
        ),

        // =========================================================================
        // 6. ЧАСТИЦЫ (PARTICLES)
        // =========================================================================
        WordEntity(
            georgian = "კი",
            transcription = "ki / ки",
            russian = "да",
            synonyms = "да, ага, конечно, так",
            partOfSpeech = "PARTICLE",
            category = "grammar",
            emoji = "✅",
            exampleGeorgian = "კი, მე მინდა წყალი.",
            exampleRussian = "Да, я хочу воды.",
            subFormsJson = forms(
                WordSubForm("კი", "ki / ки", "да / конечно", "утвердительная частица", true)
            )
        ),
        WordEntity(
            georgian = "არა",
            transcription = "ara / ара",
            russian = "нет",
            synonyms = "нет, неа, не",
            partOfSpeech = "PARTICLE",
            category = "grammar",
            emoji = "❌",
            exampleGeorgian = "არა, გმადლობთ, არ მინდა.",
            exampleRussian = "Нет, спасибо, не нужно.",
            subFormsJson = forms(
                WordSubForm("არა", "ara / ара", "нет", "отрицательная частица", true)
            )
        ),
        WordEntity(
            georgian = "არ",
            transcription = "ar / ар",
            russian = "не",
            synonyms = "не, нет",
            partOfSpeech = "PARTICLE",
            category = "grammar",
            emoji = "🚫",
            exampleGeorgian = "მე არ ვსვამ ცივ წყალს.",
            exampleRussian = "Я не пью холодную воду.",
            subFormsJson = forms(
                WordSubForm("არ", "ar / ар", "не (перед глаголом)", "отрицательная частица", true)
            )
        ),
        WordEntity(
            georgian = "ხომ",
            transcription = "khom / хом",
            russian = "ведь / разве",
            synonyms = "ведь, разве, неужели, верно",
            partOfSpeech = "PARTICLE",
            category = "grammar",
            emoji = "🧐",
            exampleGeorgian = "ეს ხომ გემრიელია?",
            exampleRussian = "Это ведь вкусно?",
            subFormsJson = forms(
                WordSubForm("ხომ", "khom / хом", "ведь / разве", "усилительно-вопросительная частица", true)
            )
        ),

        // =========================================================================
        // 7. НАРЕЧИЯ (ADVERBS)
        // =========================================================================
        WordEntity(
            georgian = "ძალიან",
            transcription = "dzalian / дзалиан",
            russian = "очень",
            synonyms = "очень, весьма, крайне, сильно",
            partOfSpeech = "ADVERB",
            category = "grammar",
            emoji = "🔥",
            exampleGeorgian = "ეს კერძი ძალიან გემრიელია.",
            exampleRussian = "Это блюдо очень вкусное.",
            subFormsJson = forms(
                WordSubForm("ძალიან", "dzalian / дзалиан", "очень", "наречие степени", true)
            )
        ),
        WordEntity(
            georgian = "აქ",
            transcription = "aq / ак",
            russian = "здесь",
            synonyms = "здесь, тут, сюда",
            partOfSpeech = "ADVERB",
            category = "grammar",
            emoji = "📍",
            exampleGeorgian = "აქ არის ჩემი მეგობარი.",
            exampleRussian = "Здесь находится мой друг.",
            subFormsJson = forms(
                WordSubForm("აქ", "aq / ак", "здесь / тут", "наречие места", true)
            )
        ),
        WordEntity(
            georgian = "დღეს",
            transcription = "dghes / дгхес",
            russian = "сегодня",
            synonyms = "сегодня, нынче, сейчас",
            partOfSpeech = "ADVERB",
            category = "grammar",
            emoji = "📅",
            exampleGeorgian = "დღეს მე მივდივარ კაფეში.",
            exampleRussian = "Сегодня я иду в кафе.",
            subFormsJson = forms(
                WordSubForm("დღეს", "dghes / дгхес", "сегодня", "наречие времени", true)
            )
        ),
        WordEntity(
            georgian = "სად",
            transcription = "sad / сад",
            russian = "где",
            synonyms = "где, куда",
            partOfSpeech = "ADVERB",
            category = "grammar",
            emoji = "❓",
            exampleGeorgian = "სად არის მეტრო?",
            exampleRussian = "Где находится метро?",
            subFormsJson = forms(
                WordSubForm("სად", "sad / сад", "где / куда", "вопросительное наречие", true)
            )
        ),
        WordEntity(
            georgian = "როგორ",
            transcription = "rogor / рогор",
            russian = "как",
            synonyms = "как, каким образом",
            partOfSpeech = "ADVERB",
            category = "grammar",
            emoji = "❓",
            exampleGeorgian = "გამარჯობა! როგორ ხარ?",
            exampleRussian = "Привет! Как дела?",
            subFormsJson = forms(
                WordSubForm("როგორ", "rogor / рогор", "как", "вопросительное наречие", true)
            )
        ),

        // =========================================================================
        // 8. ФОРМУЛЫ ВЕЖЛИВОСТИ И ЭТИКЕТ (PHRASES / GREETINGS)
        // =========================================================================
        WordEntity(
            georgian = "გამარჯობა",
            transcription = "gamarjoba / гамарджоба",
            russian = "здравствуйте",
            synonyms = "здравствуйте, привет, добрый день, здравствуй, приветствую",
            partOfSpeech = "PHRASE",
            category = "greetings",
            emoji = "👋",
            exampleGeorgian = "გამარჯობა! როგორ ხარ?",
            exampleRussian = "Здравствуйте! Как дела?",
            subFormsJson = forms(
                WordSubForm("გამარჯობა", "gamarjoba / гамарджоба", "здравствуйте / привет", "единственное число", true),
                WordSubForm("გამარჯობათ", "gamarjobat / гамарджобат", "здравствуйте", "вежливая форма / во множественном числе", true)
            )
        ),
        WordEntity(
            georgian = "მადლობა",
            transcription = "madloba / мадлоба",
            russian = "спасибо",
            synonyms = "спасибо, благодарю, спасибочки, большое спасибо",
            partOfSpeech = "PHRASE",
            category = "greetings",
            emoji = "🙏",
            exampleGeorgian = "დიდი მადლობა დახმარებისთვის.",
            exampleRussian = "Большое спасибо за помощь.",
            subFormsJson = forms(
                WordSubForm("მადლობა", "madloba / мадлоба", "спасибо", "основная форма", true),
                WordSubForm("გმადლობთ", "gmadlobt / гмадлобт", "благодарю вас", "вежливая форма", true),
                WordSubForm("დიდი მადლობა", "didi madloba / диди мадлоба", "большое спасибо", "усиленная форма", true)
            )
        ),
        WordEntity(
            georgian = "გთხოვთ",
            transcription = "gtkhovt / гтховт",
            russian = "пожалуйста",
            synonyms = "пожалуйста, прошу, будьте добры, прошу вас",
            partOfSpeech = "PHRASE",
            category = "greetings",
            emoji = "🤲",
            exampleGeorgian = "მომეცით მენიუ, გთხოვთ.",
            exampleRussian = "Дайте меню, пожалуйста.",
            subFormsJson = forms(
                WordSubForm("გთხოვთ", "gtkhovt / гтховт", "пожалуйста / прошу", "вежливая форма", true)
            )
        ),
        WordEntity(
            georgian = "ნახვამდის",
            transcription = "nakhvamdis / нахвамдис",
            russian = "до свидания",
            synonyms = "до свидания, пока, до встречи, прощайте",
            partOfSpeech = "PHRASE",
            category = "greetings",
            emoji = "👋",
            exampleGeorgian = "ხვალამდე, ნახვამდის!",
            exampleRussian = "До завтра, до свидания!",
            subFormsJson = forms(
                WordSubForm("ნახვამდის", "nakhvamdis / нахвамдис", "до свидания", "основная форма", true)
            )
        ),
        WordEntity(
            georgian = "ბოდიში",
            transcription = "bodishi / бодиши",
            russian = "извините",
            synonyms = "извините, простите, пардон, прошу прощения",
            partOfSpeech = "PHRASE",
            category = "greetings",
            emoji = "🙇",
            exampleGeorgian = "ბოდიში, სად არის მეტრო?",
            exampleRussian = "Извините, где находится метро?",
            subFormsJson = forms(
                WordSubForm("ბოდიში", "bodishi / бодиши", "извините / простите", "основная форма", true),
                WordSubForm("ბოდიშს გიხდით", "bodishs gikhdit / бодишс гихдит", "приношу извинения", "официальная форма", false)
            )
        ),
        WordEntity(
            georgian = "კარგად",
            transcription = "kargad / каргад",
            russian = "хорошо",
            synonyms = "хорошо, отлично, ладно, в порядке",
            partOfSpeech = "ADVERB",
            category = "greetings",
            emoji = "👍",
            exampleGeorgian = "მე ვარ კარგად, მადლობა.",
            exampleRussian = "У меня всё хорошо, спасибо.",
            subFormsJson = forms(
                WordSubForm("კარგად", "kargad / каргад", "хорошо / пока (всего хорошего)", "основная форма", true)
            )
        )
    )

    val DEFAULT_STORIES = listOf(
        StoryEntity(
            id = "story_cafe",
            titleGeorgian = "კაფეში",
            titleRussian = "В кафе",
            category = "Кафе",
            contentGeorgian = "გამარჯობა! მე ვარ თბილისში. დღეს მე მივდივარ კაფეში. აქ არის ჩემი მეგობარი. მე მინდა ცხელი ჩაი რძით და ხაჭაპური. ჩემი მეგობარი სვამს ყავას. ეს კერძი ძალიან გემრიელია! ანგარიში მოიტანეთ, გთხოვთ. დიდი მადლობა და ნახვამდის!",
            contentRussian = "Здравствуйте! Я в Тбилиси. Сегодня я иду в кафе. Здесь мой друг. Я хочу горячий чай с молоком и хачапури. Мой друг пьёт кофе. Это блюдо очень вкусное! Принесите счёт, пожалуйста. Большое спасибо и до свидания!",
            question = "რა შეუკვეთა ავტორმა? (Что заказал автор?)",
            option1 = "ცხელი ჩაი რძით და ხაჭაპური (Горячий чай с молоком и хачапури)",
            option2 = "ღვინო და ხინკალი (Вино и хинкали)",
            option3 = "მხოლოდ ცივი წყალი (Только холодную воду)",
            option4 = "ყავა (Кофе)",
            correctIndex = 0
        )
    )

    val GEORGIAN_KEYBOARD_LETTERS = listOf(
        "ა", "ბ", "გ", "დ", "ე", "ვ", "ზ", "თ", "ი",
        "კ", "ლ", "მ", "ნ", "ო", "პ", "ჟ", "რ", "ს",
        "ტ", "უ", "ფ", "ქ", "ღ", "ყ", "შ", "ჩ", "ც",
        "ძ", "წ", "ჭ", "ხ", "ჯ", "ჰ"
    )
}
