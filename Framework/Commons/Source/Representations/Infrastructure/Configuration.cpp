/*
 * @file Configuration.cpp
 *
 * @author <a href="mailto:krause@informatik.hu-berlin.de">Thomas Krause</a>
 * @author <a href="mailto:xu@informatik.hu-berlin.de">Xu Yuan</a>
 * @breief the gloabl configuration for NaoTH framework
 *
 */

#include "Configuration.h"
#include "Tools/DataConversion.h"
#include "Tools/Debug/NaoTHAssert.h"

#include <iostream>
#include <fstream>
#include <string.h>

using namespace naoth;

Configuration::Configuration()
{
  publicKeyFile = g_key_file_new();
  privateKeyFile = g_key_file_new();
}

Configuration::Configuration(const Configuration& orig)
{
  publicKeyFile = g_key_file_new();
  privateKeyFile = g_key_file_new();

  // copy public key
  gsize bufferLength;
  gchar* buffer = g_key_file_to_data(orig.publicKeyFile, &bufferLength, NULL);
  g_key_file_load_from_data(publicKeyFile, buffer, bufferLength, G_KEY_FILE_NONE, NULL);
  g_free(buffer);

  // copy private key
  buffer = g_key_file_to_data(orig.privateKeyFile, &bufferLength, NULL);
  g_key_file_load_from_data(privateKeyFile, buffer, bufferLength, G_KEY_FILE_NONE, NULL);
  g_free(buffer);
}

Configuration::~Configuration()
{
  if (publicKeyFile != NULL)
  {
    g_key_file_free(publicKeyFile);
  }
  if (privateKeyFile != NULL)
  {
    g_key_file_free(privateKeyFile);
  }
}

void Configuration::loadFromDir(std::string dirlocation,
                                const std::string& platform,
                                const std::string& scheme,
                                const std::string& bodyID,
                                const std::string& headID,
                                const std::string& robotName)
{
  if (!g_str_has_suffix(dirlocation.c_str(), "/"))
  {
    dirlocation = dirlocation + "/";
  }

  if (g_file_test(dirlocation.c_str(), G_FILE_TEST_EXISTS) && g_file_test(dirlocation.c_str(), G_FILE_TEST_IS_DIR))
  {
    loadFromSingleDir(publicKeyFile, dirlocation + "general/");
    loadFromSingleDir(publicKeyFile, dirlocation + "platform/" + platform + "/");
    if(scheme.size() > 0)
    {
      loadFromSingleDir(publicKeyFile, dirlocation + "scheme/" + scheme + "/");
    }
    loadFromSingleDir(publicKeyFile, dirlocation + "robots/" + robotName + "/");
    loadFromSingleDir(publicKeyFile, dirlocation + "robots_bodies/" + bodyID + "/");
    loadFromSingleDir(publicKeyFile, dirlocation + "robot_heads/" + headID + "/");
    privateDir = dirlocation + "private/";
    loadFromSingleDir(privateKeyFile, privateDir);
  } else
  {
    std::cout << "[WARN] Could not load configuration from " << dirlocation << ": directory does not exist" << std::endl;
  }
}

void Configuration::loadFromSingleDir(GKeyFile* keyFile, std::string dirlocation)
{
  // iterate over all files in the folder

  if (!g_str_has_suffix(dirlocation.c_str(), "/"))
  {
    dirlocation = dirlocation + "/";
  }

  GDir* dir = g_dir_open(dirlocation.c_str(), 0, NULL);
  if (dir != NULL)
  {
    const gchar* name;
    while ((name = g_dir_read_name(dir)) != NULL)
    {
      if (g_str_has_suffix(name, ".cfg"))
      {
        gchar* group = g_strndup(name, strlen(name) - strlen(".cfg"));
        std::string completeFileName = dirlocation + name;
        if (g_file_test(completeFileName.c_str(), G_FILE_TEST_EXISTS)
          && g_file_test(completeFileName.c_str(), G_FILE_TEST_IS_REGULAR))
        {
          loadFile(keyFile, completeFileName, std::string(group));
        }
        g_free(group);
      }


    }
    g_dir_close(dir);
  }
}

void Configuration::loadFile(GKeyFile* keyFile, std::string file, std::string groupName)
{
  GError* err = NULL;

  GKeyFile* tmpKeyFile = g_key_file_new();
  g_key_file_load_from_file(tmpKeyFile, file.c_str(), G_KEY_FILE_NONE, &err);
  if (err != NULL)
  {
    std::cerr << "[ERROR] " << file << ": " << err->message << std::endl;
    g_error_free(err);
  } else
  {
    // syntactically correct, check if there is only one group with the same 
    // name as the file
    bool groupOK = true;
    gsize numOfGroups;
    gchar** groups = g_key_file_get_groups(tmpKeyFile, &numOfGroups);
    for (gsize i = 0; groupOK && i < numOfGroups; i++)
    {
      if (g_strcmp0(groups[i], groupName.c_str()) != 0)
      {
        groupOK = false;
        std::cerr << "[ERROR] " << file << ": config file contains illegal group \"" << groups[i] << "\"" << std::endl;
        break;
      }
    }

    if(groupOK && numOfGroups == 1)
    {
      // copy every single value to our configuration
      gsize numOfKeys = 0;
      gchar** keys = g_key_file_get_keys(tmpKeyFile, groups[0], &numOfKeys, NULL);
      for(gsize i=0; i < numOfKeys; i++)
      {
        gchar* buffer = g_key_file_get_value(tmpKeyFile, groups[0], keys[i], NULL);
        g_key_file_set_value(keyFile, groups[0], keys[i], buffer);
        g_free(buffer);
      }
      g_strfreev(keys);
      
      std::cout << "[INFO] loaded " << file << std::endl;
    }

    g_strfreev(groups);

  }

  g_key_file_free(tmpKeyFile);
}

void Configuration::save()
{
  if (privateDir.empty())
    return;

  gsize length = 0;
  gchar** groups = g_key_file_get_groups(privateKeyFile, &length);
  for(gsize i=0; i < length; i++)
  {
    std::string groupname = std::string(groups[i]);
    std::string filename = privateDir + groupname + ".cfg";
    saveFile(privateKeyFile, filename, groupname );
  }
  g_strfreev(groups);
}

void Configuration::saveFile(GKeyFile* keyFile, const std::string& file, const std::string& group)
{
  GKeyFile* tmpKeyFile = g_key_file_new();
  gsize numOfKeys = 0;
  gchar** keys = g_key_file_get_keys(keyFile, group.c_str(), &numOfKeys, NULL);
  for(gsize i=0; i < numOfKeys; i++)
  {
    GError* err = NULL;
    gchar* buffer = g_key_file_get_value(keyFile, group.c_str(), keys[i], &err);
    if(err != NULL)
    {
      std::cout << "[WARN] " << err->message << std::endl;
    }
    g_key_file_set_value(tmpKeyFile, group.c_str(), keys[i], buffer);
    g_free(buffer);
  }
  GError* err = NULL;
  gsize dataLength;
  gchar* data = g_key_file_to_data(tmpKeyFile, &dataLength, &err);
  if(err == NULL)
  {
    if(dataLength > 0)
    {
      std::ofstream outFile(file.c_str(), std::ios::out);
      if(outFile.is_open()) {
        outFile.write(data, dataLength);
        outFile.close();
      } else {
        std::cerr << "[ERROR] could not open the file " << file << std::endl;
      }
      g_free(data);
    }
  }
  else
  {
    std::cerr << "[ERROR] could not save configuration file " << file << ": " << err->message << std::endl;
    g_error_free(err);
  }

  g_key_file_free(tmpKeyFile);
}


std::set<std::string> Configuration::getKeys(const std::string& group) const
{

  std::set<std::string> result;

  // public keys
  gsize length = 0;
  gchar** keys = g_key_file_get_keys(publicKeyFile, group.c_str(), &length, NULL);
  for(gsize i=0; i < length; i++)
  {
    result.insert(std::string(keys[i]));
  }
  g_strfreev(keys);

  // private keys
  length = 0;
  keys = g_key_file_get_keys(privateKeyFile, group.c_str(), &length, NULL);
  for(gsize i=0; i < length; i++)
  {
    result.insert(std::string(keys[i]));
  }
  g_strfreev(keys);
  return result;
}

bool Configuration::hasKey(const std::string& group, const std::string& key) const
{
  return ( g_key_file_has_key(publicKeyFile, group.c_str(), key.c_str(), NULL) > 0 )
      || ( g_key_file_has_key(privateKeyFile, group.c_str(), key.c_str(), NULL) > 0 );
}

bool Configuration::hasGroup(const std::string& group) const
{
  return ( g_key_file_has_group(publicKeyFile, group.c_str()) > 0 )
      || ( g_key_file_has_group(privateKeyFile, group.c_str()) > 0 );
}

std::string Configuration::getString(const std::string& group, const std::string& key) const
{
  GKeyFile* keyFile = chooseKeyFile(group, key);

  gchar* buf = g_key_file_get_string(keyFile, group.c_str(), key.c_str(), NULL);
  if (buf != NULL)
  {
    std::string result(buf);
    g_free(buf);
    return result;
  }
  return "";
}

void Configuration::setString(const std::string& group, const std::string& key, const std::string& value)
{
  g_key_file_set_string(privateKeyFile, group.c_str(), key.c_str(), value.c_str());
}

void Configuration::setDefault(const std::string& group, const std::string& key, const std::string& value)
{
  g_key_file_set_string(publicKeyFile, group.c_str(), key.c_str(), value.c_str());
}

std::string Configuration::getRawValue(const std::string& group, const std::string& key) const
{
  GKeyFile* keyFile = chooseKeyFile(group, key);

  gchar* buf = g_key_file_get_value(keyFile, group.c_str(), key.c_str(), NULL);
  if (buf != NULL)
  {
    std::string result(buf);
    g_free(buf);
    return result;
  }
  return "";
}

void Configuration::setRawValue(const std::string& group, const std::string& key, const std::string& value)
{
  g_key_file_set_value(privateKeyFile, group.c_str(), key.c_str(), value.c_str());
}

int Configuration::getInt(const std::string& group, const std::string& key) const
{
  GKeyFile* keyFile = chooseKeyFile(group, key);
  return g_key_file_get_integer(keyFile, group.c_str(), key.c_str(), NULL);
}

void Configuration::setInt(const std::string& group, const std::string& key, int value)
{
  g_key_file_set_integer(privateKeyFile, group.c_str(), key.c_str(), value);
}

void Configuration::setDefault(const std::string& group, const std::string& key, int value)
{
  g_key_file_set_integer(publicKeyFile, group.c_str(), key.c_str(), value);
}

void Configuration::setDefault(const std::string& group, const std::string& key, unsigned int value)
{
  // todo check it!
  int tmp = (int)value;
  //ASSERT(tmp > 0);
  g_key_file_set_integer(publicKeyFile, group.c_str(), key.c_str(), tmp);
}

double Configuration::getDouble(const std::string& group, const std::string& key) const
{
  GKeyFile* keyFile = chooseKeyFile(group, key);
  return g_key_file_get_double(keyFile, group.c_str(), key.c_str(), NULL);
}

void Configuration::setDouble(const std::string& group, const std::string& key, double value)
{
  //g_key_file_set_double(privateKeyFile, group.c_str(), key.c_str(), value);
  // the function above produce unecessary zeros
  g_key_file_set_string(privateKeyFile, group.c_str(), key.c_str(), DataConversion::toStr(value).c_str());
}

void Configuration::setDefault(const std::string& group, const std::string& key, double value)
{
  g_key_file_set_string(publicKeyFile, group.c_str(), key.c_str(), DataConversion::toStr(value).c_str());
}

bool Configuration::getBool(const std::string& group, const std::string& key) const
{
  GKeyFile* keyFile = chooseKeyFile(group, key);
  return g_key_file_get_boolean(keyFile, group.c_str(), key.c_str(), NULL) > 0;
}

void Configuration::setBool(const std::string& group, const std::string& key, bool value)
{
  g_key_file_set_boolean(privateKeyFile, group.c_str(), key.c_str(), value);
}

void Configuration::setDefault(const std::string& group, const std::string& key, bool value)
{
  g_key_file_set_boolean(publicKeyFile, group.c_str(), key.c_str(), value);
}
