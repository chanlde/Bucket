package com.tji.bucket.data.repository

import android.util.Log
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.Switch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LinkRepo : LinkRepository {

    private val _links = MutableStateFlow<List<LinkDevice>>(emptyList())
    override val links: StateFlow<List<LinkDevice>> = _links.asStateFlow()

    override suspend fun updateLinkDevice(linkDevice: LinkDevice) {
        _links.update { current ->
            val updatedList = current.toMutableList()
            val existingIndex = current.indexOfFirst { it.serial_number == linkDevice.serial_number }
            if (existingIndex >= 0) {
                updatedList[existingIndex] = linkDevice
            } else {
                updatedList.add(linkDevice)
            }
            updatedList
        }
    }

    override suspend fun addSubDevice(linkSn: String, switch: Switch) {
        _links.update { current ->
            current.map { linkDevice ->
                if (linkDevice.serial_number == linkSn) {
                    linkDevice.copy(subDevices = linkDevice.subDevices + switch)
                } else {
                    linkDevice
                }
            }
        }
    }

    override suspend fun removeSubDevice(linkSn: String, switchSn: String) {
        _links.update { current ->
            current.map { linkDevice ->
                if (linkDevice.serial_number == linkSn) {
                    linkDevice.copy(
                        subDevices = linkDevice.subDevices.filterNot { it.serialNumber == switchSn }
                    )
                } else {
                    linkDevice
                }
            }
        }
    }

    override suspend fun updateLinkDeviceStatus(serialNumber: String, isOnline: Boolean) {
        _links.update { current ->
            current.map { link ->
                if (link.serial_number == serialNumber) {
                    link.copy(isOnline = isOnline)
                } else {
                    link
                }
            }
        }
    }

    override suspend fun updateSubDevice(linkSn: String, updatedSwitch: Switch) {
        _links.update { current ->
            current.map { link ->
                if (link.serial_number == linkSn) {
                    link.copy(
                        subDevices = link.subDevices.map { switch ->
                            if (switch.serialNumber == updatedSwitch.serialNumber) {
                                updatedSwitch
                            } else {
                                switch
                            }
                        }
                    )
                } else {
                    link
                }
            }
        }
    }

}