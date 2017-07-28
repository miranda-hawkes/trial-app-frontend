/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import forms.TransactionForm
import play.api.data.Form
import play.api.mvc.{AnyContent, Request}

case class TransactionModel(id: Int,
                            csrfToken: String)

object TransactionModel {

  def apply(csrfToken: String): TransactionModel = TransactionModel(0, csrfToken)

  //def apply(id: Int, csrfToken: String): TransactionModel = TransactionModel(id, csrfToken)

  def emptyForm(csrfToken: String)(implicit request: Request[AnyContent]): Form[TransactionModel] = {
    TransactionForm.transactionForm.fill(
      TransactionModel(1, csrfToken)
    )
  }
}
