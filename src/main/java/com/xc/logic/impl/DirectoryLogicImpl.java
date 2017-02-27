package com.xc.logic.impl;

import com.xc.dao.DirectoryDao;
import com.xc.entity.Directory;
import com.xc.logic.DirectoryLogic;
import com.xc.logic.NoteLogic;
import com.xc.util.GenerateUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by yb on 2017/2/25 0025.
 */
@Service
public class DirectoryLogicImpl implements DirectoryLogic {
	@Autowired
	private DirectoryDao directoryDao;
	@Autowired
	private NoteLogic noteLogic;

	@Override
	public String createDir(Directory directory) {
		String id = GenerateUUID.getUUID32();
		directory.setId(id);
		Date date = new Date();
		directory.setCreateTime(date);
		directory.setModifyTime(date);
		directoryDao.insert(directory);
		return id;
	}

	@Override
	public boolean modifyDir(Directory directory) {
		// 判断是否存在
		Directory oldDir = getDirById(directory.getId());
		if (oldDir == null) {
			return false;
		}
		oldDir.setModifyTime(new Date());
		oldDir.setName(directory.getName());
		oldDir.setParentId(directory.getParentId());
		directoryDao.update(oldDir);
		return true;
	}

	@Override
	public Directory getDirById(String id) {
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		return directoryDao.selectDirById(id);
	}

	@Override
	public List<Directory> getDirsByParentId(String id) {
		return directoryDao.selectDirsByParentId(id);
	}

	@Override
	@Transactional
	public void removeDir(String id) {
		if (StringUtils.isEmpty(id)) {
			return;
		}
		// 删除子目录及子目录下面的笔记
		List<Directory> subDirs = getDirsByParentId(id);
		if (subDirs != null && subDirs.size() > 0) {
			for (Directory subDir : subDirs) {
				removeDir(subDir.getId());
			}
		}
		// 删除目录下的笔记
		noteLogic.removeNotesByDirId(id);
		// 删除目录
		directoryDao.delete(id);
	}
}