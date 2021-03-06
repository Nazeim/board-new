package org.ergoplatform.board.protocol

import org.ergoplatform.board.models.{SignedData, VoteRecord}
import play.api.libs.json.Json

case class VoteView(electionId: String,
                    groupId: String,
                    sectionId: String,
                    index: Long,
                    hash: String,
                    m: String,
                    signedDataByVoter: SignedData,
                    signedDataByBoard: SignedData,
                    timestamp: Long)

object VoteView {
  implicit val fmt = Json.format[VoteView]

  def fromRecord(vr: VoteRecord): VoteView = VoteView(
    electionId = vr.electionId.toString,
    groupId = vr.groupId,
    sectionId = vr.sectionId,
    index = vr.index,
    m = vr.m,
    hash = vr.hash,
    signedDataByVoter = vr.signedDataByVoter,
    signedDataByBoard = vr.signedDataByBoard,
    timestamp = vr.timestamp
  )
}
