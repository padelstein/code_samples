"""
k-means clusterer

author: David Kauchak, Grace Benz, Patrick Adelstein
date: April, 2013
"""

import random
import sets
from datapoint import *
import copy

    
def k_plus_plus_initialization(data, k, distance_metric):
  ''' returns a list of centers using k++ approach '''
  
  centers = []
  
  data = copy.deepcopy(data)
  
  # pick a random datapoint as our first center
  first_center_index = random.randint(0, len(data) - 1)
  centers.append( data[ first_center_index ] )
  print "added a new center. length of centers is " + str(len(centers))
  data.pop( first_center_index )  # remove from *data* so we don't use it again
  
  # find the k-1 other centers
  for i in range(k-1):
   
    # need to keep track of the total probability density for this iteration
    total_density = 0.0 
    
    # we'll keep track of the weights for each datapoint in a list (we were using a dictionary but it wasn't working. Maybe the hash function was off?)
    datapoint_weights = []
    
    # find the distance**2 to the farthest center for each datapoint
    for i in range(len(data)):
      max_distance = 0
      
      # now find the farthest center
      for center in centers:
        if distance_metric == 1:
          distance = data[i].euclidean(center)**2
        else:
          distance = data[i].cosine(center)**2
        
        if distance > max_distance:
          max_distance = distance
      
      total_density += max_distance # keeping track of the total density
      datapoint_weights.append(max_distance)
    
    ''' 
    general strat here is to now pick a random number within our total density
    and then find which datapoint that number corresponds to.
    inspired by a post on stack overflow
    '''
    r = random.uniform(0, total_density) # our random number
  
    step = 0.0 # this will keep track of where in the density we are looking
    
    for k in range(len(data)):
      
      weight = datapoint_weights[k]
      
      # if r is within this certain range of the probability density
      if (step + weight) >= r:
        centers.append(data[k])
        data.pop(k)
        print " random number is " + str(r) + " added a new center. length of centers is " + str(len(centers))
        break
      else:
        step += weight
  
  return centers


def random_datapoint_initialization(data, k):
  ''' returns a list of centers given data and k '''
  centers = []
  usedCenters = set() # keep track of the index (from the list of datapoints) of data points used as initial centers
 
  # choose k centers
  for i in range(k):
    
    # pick some random center from the data
    j = random.randint(0, len(data)-1)
    
    # make sure we haven't already used it 
    while j in usedCenters:
      j = random.randint(0, len(data)-1)
    
    usedCenters.add(j)  # add it to our list of used datapoints
    centers.append( data[j] ) # add it to our initial list of centers
    
  return centers

class KMeans:
  """
  A class for doing k-means clustering of a data set
  """

  INFINITY = 1.0e400

  # constants for different distance measures
  EUCLIDEAN = 1
  COSINE = 2

  def __init__(self, data, k, distance_metric=EUCLIDEAN):
    """
    Create a new k-means cluster to cluster *data* into *k*
    clusters.  By default the clustering will use euclidean
    distance, but cosine distance can also be used.

    data should be a list of DataPoints.

    clustering does NOT begin until the cluster method is called.
    """

    self.data = data
    self.k = k
    self.distance_metric = distance_metric
    
    # initialize list of centers using random datapoints
    self.centers = random_datapoint_initialization(data, k)  
    
    # initialize list of centers using k++
    #self.centers = k_plus_plus_initialization(data, k, distance_metric) 
    
    self.clusters = [] # initialize list of centers
    
    for i in range(k):
      self.clusters.append([])
    
  def cluster(self, iterations=INFINITY):
    """
    Cluster the data and return the found clusters.

    By default, the clustering will continue until the clusters converge,
    i.e. don't change. Optionally, you can specify a fixed number of iterations.
    """
    # run algorithm certain number of times 
    if iterations < self.INFINITY:
      for i in range(iterations):
        self.assign_to_centers()
        self.centers = self.recalculate_centers()
    # or run until centers converge
    else:
      counter = 0
      last_centers = copy.deepcopy(self.centers) # keep track of the previous list of centers
      
      # run k-means i.e. recalculate self.centers
      self.assign_to_centers()
      self.centers = self.recalculate_centers()
      
      counter += 1
      print counter

      # keep running it until the current list of centers is the same as the previous or we've reached a stable state
      while last_centers != self.centers and counter < 50:
        last_centers = copy.deepcopy(self.centers) # have to be sure to keep track of the previous
        
        # run k-means i.e. recalculate self.centers
        self.assign_to_centers()
        self.centers = self.recalculate_centers()
        
        counter += 1
        print counter
      
    return self.clusters, counter

  def assign_to_centers(self):
    """
    Assign the points to the cluster centers.  This will
    update the clusters.
    """
    print "k is " + str(self.k) + " and num of centers is " + str(len(self.centers))
   
    # clear *clusters* since this a new clustering
    for i in range(self.k):
      self.clusters[i] = []

    # for every datapoint find the closest center
    for point in self.data:
      
      min_distance = self.INFINITY
      # compare the current datapoint to all the centers
      for j in range(self.k):
        
        if self.distance_metric == 1:
          distance = point.euclidean( self.centers[j] )
          print "using euclidean"
        elif self.distance_metric == 2:
          distance = point.cosine_distance( self.centers[j] )
          print "using cosine"
        
        # update the datapoint's closest center if appropriate
        if distance < min_distance:
          min_distance = distance
          min_center = j 
      
      # add current datapoint to appropriate cluster (*clusters* is indexed to correspond to how *centers* is indexed)
      self.clusters[min_center].append(point) 

  def recalculate_centers(self):
    """
    Recalculate the cluster centers based on the current clusters.
    """
    centers = []

    # calculate the mean of all the datapoints in each cluster (k clusters)
    for i in range(self.k):
      
      print "recalculating the " + str(i) + "center"
      
      # ran into a case where a cluster was empty... use the old center?
      if len(self.clusters[i]) == 0:
        print "cluster was empty"
        centers.append(self.centers[i])
      
      else:
      
        new_center = WordDataPoint(dict(x=0)) # starting point, just 0
        #new_center = DataPoint(dict(x=0))
        
        # add all datapoints in this cluster together
        for point in self.clusters[i]:
          new_center.add_data_counts(point)
       
        # divide by number of points in the clusters list 
        new_center.divide_by_constant(len(self.clusters[i]))
        print "added a new center to centers"
        centers.append(new_center)  # update the current center
    
    self.centers = centers
    return centers

  
  def get_clusters(self):
    """
    Return the current clustering of the data.  The data structure
    returned should be a list of lists, specifically each cluster
    will be represented as a list of DataPoints and you'll be returning
    a list of clusters.
    """

    return self.clusters


  def get_centers(self):
    """ Return the current cluster centers as a list of DataPoints. """

    return self.centers