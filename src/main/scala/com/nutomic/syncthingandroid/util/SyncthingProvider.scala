package com.nutomic.syncthingandroid.util

import java.io.FileNotFoundException

import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.database.{Cursor, MatrixCursor}
import android.os.{CancellationSignal, IBinder, ParcelFileDescriptor}
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import com.nutomic.syncthingandroid.R
import com.nutomic.syncthingandroid.syncthing.{SyncthingService, SyncthingServiceBinder}

import scala.collection.JavaConversions._

class SyncthingProvider extends DocumentsProvider with ServiceConnection {

  private val Tag = "SyncthingProvider"

  val DefaultRootProjection = Array(Root.COLUMN_ROOT_ID, Root.COLUMN_TITLE, Root.COLUMN_SUMMARY,
    Root.COLUMN_DOCUMENT_ID, Root.COLUMN_ICON)

  private var syncthingService: SyncthingService = _

  def onCreate: Boolean = {
    // TODO: return false if syncthing not running (check user settings)
    getContext.bindService(new Intent(getContext, classOf[SyncthingService]), this,
      Context.BIND_AUTO_CREATE)
    true
  }

  def onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
    syncthingService = iBinder.asInstanceOf[SyncthingServiceBinder].getService
  }

  def onServiceDisconnected(componentName: ComponentName) {
    syncthingService = null
  }

  private def isInitialized = syncthingService != null && syncthingService.getApi != null

  def queryRoots(projection: Array[String]): Cursor = {
    val result = new MatrixCursor(DefaultRootProjection)

    syncthingService.getApi.getFolders.foreach { folder =>
      val row = result.newRow()
      row.add(Root.COLUMN_ROOT_ID, folder.id)
      row.add(Root.COLUMN_TITLE, folder.id)
      row.add(Root.COLUMN_SUMMARY, folder.path)
      row.add(Root.COLUMN_DOCUMENT_ID, folder.id)
      row.add(Root.COLUMN_ICON, R.drawable.ic_launcher)
    }

    result
  }

  @throws(classOf[FileNotFoundException])
  def queryChildDocuments(parentDocumentId: String, projection: Array[String],
                          sortOrder: String): Cursor = {
    // TODO: browse
    null
  }

  @throws(classOf[FileNotFoundException])
  def queryDocument(documentId: String, projection: Array[String]): Cursor = {
    // TODO: return metadata
    null
  }

  @throws(classOf[FileNotFoundException])
  def openDocument(documentId: String, mode: String, signal:
                   CancellationSignal): ParcelFileDescriptor = {
    // TODO: open file, handle mode w as unsupported
    null
  }
}
