package org.ergoplatform.board

import org.ergoplatform.board.models.{MongoId, SignedData, VoteRecord}
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.HashService
import org.ergoplatform.board.stores.VoteStore
import play.api.libs.json.Json

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InMemoryVoteStore extends VoteStore with FutureHelpers {

  val data = TrieMap[MongoId, VoteRecord]()

  override def create(electionId: MongoId, cmd: VoteCreate, boardSign: SignedData) = {
    val id = MongoId()
    getIndexFor(electionId).flatMap{ case (index, prevHash) =>
      val timestamp = System.currentTimeMillis()
      val hash = HashService.hash(Json.stringify(Json.toJson(cmd)), Some(prevHash).filterNot(_.isEmpty))
      val vote = VoteRecord(id,
        electionId,
        cmd.groupId,
        cmd.sectionId,
        index + 1,
        hash,
        cmd.m,
        cmd.signature,
        boardSign,
        timestamp)
      data.put(id, vote)
      vote.asFut
    }
  }

  override def getAllByElectionId(electionId: MongoId, offset: Int, limit: Int) = data
    .filter(_._2.electionId == electionId).values.slice(offset, offset + limit).toList.asFut

  override def countByElectionId(electionId: MongoId) = data.count(_._2.electionId == electionId).asFut

  def getIndexFor(electionId: MongoId): Future[(Long, String)] = data.filter(_._2.electionId == electionId).values.map { v =>
    (v.index, v.hash)
  }.toList.sortBy(_._1).lastOption.getOrElse((0L, "")).asFut
}
