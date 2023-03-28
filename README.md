# Курсова робота студента групи АІ-216 Семеренко Микити
## Тема курсової роботи
Телеграм-бот, за допомогою якого можна замовити доставку їжі з ресторану.
## Завдання курсової роботи
Розробка телеграм-бота, основною функцією якого є надання можливості замовлення доставки їжі з ресторану.
## Бачення системи
Система, яка дозволяє замовити доставку їжі з ресторану. За допомогою меню можна обирати необхідні страви та додавати їх до кошику, при необхідності отримувати більше інформації про них. При оформленні заказу необхідно заповнити форму з контактними даними та адресою доставки. В результаті оформлення замовлення, створюється новий заказ, який передається менеджеру для обробки
## Користувацькі ролі
Клієнт - має можливість переглядати меню, обирати страви з нього, отримувати про них більш детальну інформацію, додавати їх до кошика та оформлювати замовлення для доставки.
Гість - клієнт, який не зареєструвався в системі та не має клієнтського акаунту, через що не зберігається його історія замовлень та персональні дані.
Менеджер - переглядає новостворені замовлення, спілкується з клієнтом щодо їх коректності та актуальності, змінює стан замовлення відповідного до його готовності.
Адміністратор - має можливість додавати правки до меню, надавати доступ до системи новим менеджерам, забезпечує актуальність інформації в системі.
## Користувальницькі історії:
1. Як гість, я можу додати обрану страву до кошику, щоб мати змогу оформити замовлення. (1)
2. Як гість, я можу створити акаунт клієнта, щоб зберігалися мої персональні дані та історія замовлень. (2)
   - Потрібна адреса електронної пошти та пароль, а також необхідно надати свою персональну інформацію, таку як ПІБ, номер телефону та інше.
      - Тест 1: На одну пошту реєструється лише один користувач;
      - Тест 2: Номер телефону має бути унікальним для кожного акаунту;
      - Тест 3: Пароль має складатися мінімум з 6 символів, в складі яких повинні бути цифри, літери та спеціальні символи
3. Як клієнт, я можу переглядати більш детальну інформацію про страву, щоб краще розуміти чи варто її замовляти. (1)
4. Як клієнт, я можу зайти в свій акаунт, щоб мої персональні дані збереглися та не було потрібним вводити їх кожного разу при оформленні замовлення. (2)
   - Необхідно ввести адресу електронної пошти або номер телефону та пароль користувача
5. Як клієнт, я можу переглянути історію своїх замовлень, щоб згадати, що саме я замовляв в минулому. (4)
6. Як менеджер, я можу переглядати інформацію про замовлення, щоб при спілкуванні з клієнтом перевірити коректність цих даних. (2)
   - Для цього необхідно зайти в акаунт менеджера, який був створений адміністратором
      - Тест 1: Використання корпоративної пошти для реєстрації менеджерів в системі
      - Тест 2: Використання логіну та паролю, наданих адміністратором, для входу в профіль менеджера закладу
7. Як менеджер, я можу змінювати стан замовлення, відповідно до його готовності, щоб не актуальні замовлення перейшли до архіву та не оброблялися іншими менеджерами. (3)
    - Тест 1: Необхідна наявність поля стан замовлення, яке буде змінюватися на різних етапах його формування
8. Як менеджер, я можу змінювати інформацію про замовлення, щоб дані про них були коректними. (3)
9. Як адміністратор, я можу змінювати інформацію про страви, щоб зберігати актуальність інформації. (4)
10. Як адміністратор, я можу створювати нові акаунти для менеджерів, щоб надати їм можливість обробляти замовлення. (4)
