# Name: Patrick Adelstein and Grace Benz
# Date: March 20, 2013
# Description: An improved Naive Bayes Classifier
#
#

import math, os, pickle
from DataReader import *
from decimal import *

class BayesClassifierBest:

	def __init__(self):
		'''This method initializes the  improved Naive Bayes classifier'''

		self.total_docs = 0
		
		self.lamb = 0.4
		# a dictionary that maps labels to frequency of that label
		self.label_dictionary = {}
		
		# a dictionary that maps labels to a dictionary that maps unigrams to frequencies
		self.master_unigram_dictionary = {}
		# a dictionary that maps labels to how many non-unique unigrams appear in the data for that label
		self.unigram_dictionary = {}
		# a set that keep tracks of our unigram vocabulary across all labels
		self.unigram_vocab = set()
		
		# a dictionary that maps labels to a dictionary that maps bigrams to frequencies
		self.master_bigram_dictionary = {}
		# a dictionary that maps labels to how many non-unique bigrams appear in the data for that label
		self.bigram_dictionary = {}
		# a set that keep tracks of our bigram vocabulary across all labels
		self.bigram_vocab = set()


	def train(self, dataFile):
		'''Trains the Naive Bayes Sentiment Classifier.'''
		
		reader = DataReader(dataFile)
		
		# go through all the docs in our corpus
		for doc in reader:
			(label, data) = doc
			self.total_docs += 1
			
			# if we haven't seen this label at all yet
			if label not in self.label_dictionary:
				
				self.label_dictionary[label] = 1				# note that we have seen one document of that label
				
				self.master_unigram_dictionary[label] = {}	 			# add a new dictionary to the master dictionary under that label 
				self.master_bigram_dictionary[label] = {}
				
				self.unigram_dictionary[label] = 0				# note that we have seen 0 words of that label
				self.bigram_dictionary[label] = 0
			else:
				self.label_dictionary[label] += 1				# increment our count of the documents of this label 
				
			# we now have to count the actual words in this doc 
			unigram_dict = self.master_unigram_dictionary[label]
			bigram_dict = self.master_bigram_dictionary[label]		
			
			# train on the unigrams
			for unigram in data:

				self.unigram_vocab.add( unigram )
				
				# if we have not seen this unigram under this label
				if unigram not in unigram_dict:
					unigram_dict[unigram] = 1					# note that we have seen the unigram once under this label
				else:
					unigram_dict[unigram] += 1					# increment the count of this unigram under this label
					
				self.unigram_dictionary[label] += 1			# increment our count of the total non-unique unigrams under this label
			
			# train on the bigrams
			for i in range(len(data)-1):
				bigram = (data[i], data[i+1])

				self.bigram_vocab.add( bigram )
				
				# if we have not seen this bigram under this label
				if bigram not in bigram_dict:
					bigram_dict[bigram] = 1					# note that we have seen the bigram once under this label
				else:
					bigram_dict[bigram] += 1					# increment the count of this bigram under this label
					
				self.bigram_dictionary[label] += 1			# increment our count of the total non-unique bigrams under this label
				
		self.save(dataFile + ".best.pickled")



	def classify(self, sText):
		'''Given a target string sText, this function returns the most likely document
		class to which the target string belongs (i.e., positive or negative ).
		'''
		if isinstance(sText, basestring):
			sText = tokenize(sText)

		arg_max = -1.0e400
		max_label = ""
		
		# for each label we have seen compute a current_arg and keep track of the max
		for label in self.label_dictionary:
			# this is 0 now because we are taking the sum of logs of probabilities
			current_arg = 0
			
			# calculate the prior of the current label and add the log to our current arg
			current_arg += math.log( float( self.label_dictionary[label] ) / float( self.total_docs ) )
			
			# for each unigram in sText
			for unigram in sText:
				# if we have seen this unigram in the training data for this label
				if unigram in self.master_unigram_dictionary[label]:
					# calculate and add the log of the conditional probability for unigram using laplacean smoothing with lambda
					current_arg += math.log( float( self.master_unigram_dictionary[label][unigram] + self.lamb ) / float( self.unigram_dictionary[label] + (self.lamb * len(self.unigram_vocab)) ) )
				# else if we have seen it in the entire training data
				elif unigram in self.unigram_vocab:
					current_arg += math.log ( self.lamb / float( self.unigram_dictionary[label] + (self.lamb * len(self.unigram_vocab)) ) )
			
			# for each bigram in sText
			for i in range(len(sText)-1):
				bigram = (sText[i], sText[i+1])
				# if we have seen this bigram in the training data for this label
				if bigram in self.master_bigram_dictionary[label]:
					# calculate and add the log of the conditional probability for bigram using laplacean smoothing with lambda
					current_arg += math.log( float( self.master_bigram_dictionary[label][bigram] + self.lamb ) / float( self.bigram_dictionary[label] + (self.lamb * len(self.bigram_vocab)) ) )
				# else if we have seen it in the entire training data
				elif bigram in self.bigram_vocab:
					current_arg += math.log ( self.lamb / float( self.bigram_dictionary[label] + (self.lamb * len(self.bigram_vocab)) ) )
			
			# keep track of the max arg we have seen and update accordingly
			if current_arg > arg_max:
				arg_max = current_arg
				max_label = label
				
		
		return max_label

	def save(self, sFilename):
		'''Save the learned data during training to a file using pickle.'''
		
		f = open(sFilename, "w")
		p = pickle.Pickler(f)
		# use dump to dump your variables
		p.dump(self.master_unigram_dictionary)
		p.dump(self.master_bigram_dictionary)
		p.dump(self.label_dictionary)
		p.dump(self.unigram_dictionary)
		p.dump(self.bigram_dictionary)
		p.dump(self.total_docs)
		p.dump(self.unigram_vocab)
		p.dump(self.bigram_vocab)
		f.close()
		
	def load(self, sFilename):
		'''Given a file name of stored data, load and return the object stored in the file.'''
		f = open(sFilename, "r")
		u = pickle.Unpickler(f)
		# use load to load in previously dumped variables
		self.master_unigram_dictionary = u.load()
		self.master_bigram_dictionary = u.load()
		self.label_dictionary = u.load()
		self.unigram_dictionary = u.load()
		self.bigram_dictionary = u.load()
		self.total_docs = u.load()
		self.unigram_vocab = u.load()
		self.bigram_vocab = u.load()
		
		f.close()
		return (self.master_unigram_dictionary, self.master_bigram_dictionary, self.label_dictionary, self.unigram_dictionary, self.bigram_dictionary, self.total_docs, self.unigram_vocab, self.bigram_vocab)
